/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.generator.v10.typegenerators;

import com.google.common.base.Supplier;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GenerationException;
import org.raml.jaxrs.generator.GeneratorType;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.SchemaTypeFactory;
import org.raml.jaxrs.generator.builders.BuildPhase;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.jaxrs.generator.extension.types.MethodType;
import org.raml.jaxrs.generator.extension.types.PredefinedFieldType;
import org.raml.jaxrs.generator.extension.types.PredefinedMethodType;
import org.raml.jaxrs.generator.extension.types.TypeContext;
import org.raml.jaxrs.generator.extension.types.TypeExtension;
import org.raml.jaxrs.generator.ramltypes.GProperty;
import org.raml.jaxrs.generator.ramltypes.GType;
import org.raml.jaxrs.generator.v10.*;
import org.raml.jaxrs.generator.v10.types.V10TypeFactory;
import org.raml.v2.api.model.v10.common.Annotable;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 1/29/17. Just potential zeroes and ones
 */
public class SimpleInheritanceExtension implements TypeExtension {

  private final V10GType originalType;
  private final V10TypeRegistry registry;
  private final CurrentBuild currentBuild;

  public SimpleInheritanceExtension(V10GType originalType, V10TypeRegistry registry, CurrentBuild currentBuild) {
    this.originalType = originalType;
    this.registry = registry;
    this.currentBuild = currentBuild;
  }

  @Override
  public TypeSpec.Builder onType(TypeContext context, TypeSpec.Builder builder, V10GType objectType,
                                 BuildPhase buildPhase) {

    if (buildPhase == BuildPhase.INTERFACE) {
      return buildDeclaration(context, objectType);
    } else {
      return buildTypeImplementation(context, objectType);
    }
  }

  private TypeSpec.Builder buildTypeImplementation(TypeContext context, V10GType objectType) {

    ClassName className = originalType.javaImplementationName(context.getModelPackage());

    TypeSpec.Builder typeSpec = TypeSpec
        .classBuilder(className)
        .addModifiers(Modifier.PUBLIC);

    ClassName parentClassName = (ClassName) originalType.defaultJavaTypeName(context.getModelPackage());

    if (parentClassName != null) {
      typeSpec.addSuperinterface(parentClassName);
    }

    typeSpec = runClassExtensions(context, objectType, typeSpec, BuildPhase.IMPLEMENTATION,
                                  Annotations.ON_TYPE_CLASS_CREATION);

    if (typeSpec == null) {

      return null;
    }

    V10TypeRegistry localRegistry = registry.createRegistry();
    List<PropertyInfo> properties = new ArrayList<>();
    int internalTypeCounter = 0;
    for (GProperty declaration : originalType.properties()) {

      if (declaration.isInline()) {
        String internalTypeName = Integer.toString(internalTypeCounter);

        V10GType type =
            localRegistry.createInlineType(internalTypeName,
                                           Annotations.CLASS_NAME.get(
                                                                      Names.typeName(declaration.name(), "Type"),
                                                                      (Annotable) declaration.implementation()),
                                           (TypeDeclaration) declaration.implementation(), originalType,
                                           CreationModel.INLINE_FROM_TYPE
                );
        TypeGenerator internalGenerator = inlineTypeBuild(localRegistry, currentBuild,
                                                          GeneratorType.generatorFrom(type));
        if (internalGenerator instanceof JavaPoetTypeGenerator) {
          properties.add(new PropertyInfo(localRegistry, declaration.overrideType(type)));
          internalTypeCounter++;
        } else {
          throw new GenerationException("internal type bad");
        }
      } else {
        properties.add(new PropertyInfo(localRegistry, declaration));
      }
    }

    buildPropertiesForImplementation(context, objectType, typeSpec, properties);

    typeSpec = runClassExtensions(context, objectType, typeSpec, BuildPhase.IMPLEMENTATION, Annotations.ON_TYPE_CLASS_FINISH);

    return typeSpec;
  }

  private void buildPropertiesForImplementation(final TypeContext context, final V10GType objectType,
                                                final TypeSpec.Builder typeSpec, List<PropertyInfo> properties) {
    for (final PropertyInfo propertyInfo : properties) {

      FieldSpec.Builder fieldSpec =
          FieldSpec.builder(propertyInfo.resolve(context), Names.variableName(propertyInfo.getName()))
              .addModifiers(Modifier.PRIVATE);

      if (propertyInfo.getName().equals(objectType.discriminator())) {
        fieldSpec.initializer("$S",
                              objectType.discriminatorValue().or(calculateValue(context, objectType, typeSpec, propertyInfo)));
      }

      fieldSpec =
          context.onField(context, typeSpec, fieldSpec, objectType,
                          (V10GProperty) propertyInfo.getProperty(),
                          BuildPhase.IMPLEMENTATION, PredefinedFieldType.PROPERTY);

      fieldSpec =
          currentBuild.getFieldExtension(Annotations.ON_TYPE_FIELD_CREATION, objectType)
              .onField(context, typeSpec, fieldSpec, objectType, (V10GProperty) propertyInfo.getProperty(),
                       BuildPhase.IMPLEMENTATION, PredefinedFieldType.PROPERTY);

      if (fieldSpec != null) {

        typeSpec.addField(fieldSpec.build());
      }

      MethodSpec.Builder getSpec = MethodSpec
          .methodBuilder(Names.methodName("get", Names.typeName(propertyInfo.getName())))
          .addModifiers(Modifier.PUBLIC)
          .addStatement("return this." + Names.variableName(propertyInfo.getName()));

      getSpec.returns(propertyInfo.resolve(context));
      getSpec =
          runPropertyExtensions(context, objectType, typeSpec, getSpec, Collections.<ParameterSpec.Builder>emptyList(),
                                (V10GProperty) propertyInfo.getProperty(), BuildPhase.IMPLEMENTATION, PredefinedMethodType.GETTER
          );

      if (getSpec != null) {
        typeSpec.addMethod(getSpec.build());
      }

      if (!propertyInfo.getName().equals(objectType.discriminator())) {

        MethodSpec.Builder setSpec = MethodSpec
            .methodBuilder(Names.methodName("set", Names.typeName(propertyInfo.getName())))
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this." + Names.variableName(propertyInfo.getName()) + " = " + Names
                .variableName(propertyInfo.getName()));

        ParameterSpec.Builder parameterSpec = ParameterSpec
            .builder(propertyInfo.resolve(context), Names.variableName(propertyInfo.getName()));

        setSpec.addParameter(parameterSpec.build());

        setSpec =
            runPropertyExtensions(context, objectType, typeSpec, setSpec, Collections.singletonList(parameterSpec),
                                  (V10GProperty) propertyInfo.getProperty(), BuildPhase.IMPLEMENTATION,
                                  PredefinedMethodType.SETTER);

        if (setSpec != null) {
          typeSpec.addMethod(setSpec.build());
        }
      }
    }
  }

  private Supplier<String> calculateValue(final TypeContext context, final V10GType objectType, final TypeSpec.Builder typeSpec,
                                          final PropertyInfo propertyInfo) {
    return new Supplier<String>() {

      @Override
      public String get() {
        if (propertyInfo.resolve(context).toString().equals("java.lang.String")) {
          return originalType.defaultJavaTypeName(context.getModelPackage()).toString();
        } else {
          throw new GenerationException("while generating type " + objectType.name()
              + " discriminatorValue not defined and not of type string ( can't map java type name to anything but string )");
        }
      }
    };
  }

  private TypeSpec.Builder buildDeclaration(TypeContext context, V10GType objectType) {

    List<V10GType> parentTypes = originalType.parentTypes();
    int internalTypeCounter = 0;

    // this should be in the generator;
    List<PropertyInfo> properties = new ArrayList<>();
    V10TypeRegistry localRegistry = registry.createRegistry();
    for (GProperty declaration : originalType.properties()) {

      if (declaration.isInline()) {
        String internalTypeName = Integer.toString(internalTypeCounter);

        V10GType type =
            localRegistry.createInlineType(internalTypeName,
                                           Annotations.CLASS_NAME.get(
                                                                      Names.typeName(declaration.name(), "Type"),
                                                                      (Annotable) declaration.implementation()),
                                           (TypeDeclaration) declaration.implementation(),
                                           originalType, CreationModel.INLINE_FROM_TYPE);

        TypeGenerator internalGenerator = inlineTypeBuild(localRegistry, currentBuild,
                                                          GeneratorType.generatorFrom(type));
        if (internalGenerator instanceof JavaPoetTypeGenerator) {
          properties.add(new PropertyInfo(localRegistry, declaration.overrideType(type)));
          context.createInternalClass((JavaPoetTypeGenerator) internalGenerator);
          internalTypeCounter++;
        } else {
          throw new GenerationException("internal type bad");
        }
      } else {
        properties.add(new PropertyInfo(localRegistry, declaration));
      }

    }

    ClassName interf = (ClassName) originalType.defaultJavaTypeName(context.getModelPackage());

    TypeSpec.Builder typeSpec = TypeSpec
        .interfaceBuilder(interf)
        // .addAnnotation(AnnotationSpec.builder(RamlGenerator.class).addMember("value", "$S", "ramlforjaxrs-simple").build())
        .addModifiers(Modifier.PUBLIC);


    for (GType parentType : parentTypes) {

      if ("object".equals(parentType.name())) {

        continue;
      }

      typeSpec.addSuperinterface(parentType.defaultJavaTypeName(context.getModelPackage()));
    }

    typeSpec = runClassExtensions(context, objectType, typeSpec, BuildPhase.INTERFACE, Annotations.ON_TYPE_CLASS_CREATION);

    if (typeSpec == null) {
      return null;
    }

    buildPropertiesForInterface(context, objectType, properties, typeSpec);

    context.addImplementation();
    typeSpec = runClassExtensions(context, objectType, typeSpec, BuildPhase.INTERFACE, Annotations.ON_TYPE_CLASS_FINISH);
    return typeSpec;
  }

  private void buildPropertiesForInterface(TypeContext context, V10GType objectType,
                                           List<PropertyInfo> properties, TypeSpec.Builder typeSpec) {
    for (PropertyInfo propertyInfo : properties) {

      MethodSpec.Builder getSpec = MethodSpec
          .methodBuilder(Names.methodName("get", propertyInfo.getName()))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
      getSpec.returns(propertyInfo.resolve(context));

      getSpec =
          runPropertyExtensions(context, objectType, typeSpec, getSpec, Collections.<ParameterSpec.Builder>emptyList(),
                                (V10GProperty) propertyInfo.getProperty(), BuildPhase.INTERFACE, PredefinedMethodType.GETTER
          );

      if (getSpec != null) {
        typeSpec.addMethod(getSpec.build());
      }

      if (!propertyInfo.getName().equals(objectType.discriminator())) {
        MethodSpec.Builder setSpec = MethodSpec
            .methodBuilder(Names.methodName("set", propertyInfo.getName()))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        ParameterSpec.Builder parameterSpec = ParameterSpec
            .builder(propertyInfo.resolve(context), Names.variableName(propertyInfo.getName()));

        setSpec.addParameter(
            parameterSpec.build());

        setSpec =
            runPropertyExtensions(context, objectType, typeSpec, setSpec, Collections.singletonList(parameterSpec),
                                  (V10GProperty) propertyInfo.getProperty(), BuildPhase.INTERFACE, PredefinedMethodType.SETTER
            );

        if (setSpec != null) {
          typeSpec.addMethod(setSpec.build());
        }
      }
    }
  }


  private static TypeGenerator inlineTypeBuild(V10TypeRegistry registry, CurrentBuild currentBuild, GeneratorType type) {

    switch (type.getObjectType()) {

      case ENUMERATION_TYPE:
        return V10TypeFactory.createEnumerationType(currentBuild, type.getDeclaredType());

      case UNION_TYPE:
        return V10TypeFactory.createUnion(currentBuild, registry, (V10GType) type.getDeclaredType());

      case PLAIN_OBJECT_TYPE:
        return V10TypeFactory.createObjectType(registry, currentBuild, (V10GType) type.getDeclaredType(), false);

      case JSON_OBJECT_TYPE:
        return SchemaTypeFactory.createJsonType(currentBuild, type.getDeclaredType());

      case XML_OBJECT_TYPE:
        return SchemaTypeFactory.createXmlType(currentBuild, type.getDeclaredType());
    }

    throw new GenerationException("don't know what to do with type " + type.getDeclaredType());
  }

  private MethodSpec.Builder runPropertyExtensions(TypeContext context, V10GType objectType, TypeSpec.Builder typeSpec,
                                                   MethodSpec.Builder getSpec, List<ParameterSpec.Builder> parameters,
                                                   V10GProperty property,
                                                   BuildPhase phase,
                                                   MethodType methodType) {

    MethodSpec.Builder builder = context.onMethod(context, typeSpec, getSpec, parameters,
                                                  objectType, property, phase, methodType);

    builder = currentBuild.getMethodExtension(Annotations.ON_TYPE_METHOD_CREATION, objectType)
        .onMethod(context, typeSpec, builder, parameters, objectType, property, phase, methodType);
    return builder;
  }

  private TypeSpec.Builder runClassExtensions(TypeContext context, V10GType objectType, TypeSpec.Builder typeSpec,
                                              BuildPhase buildPhase, Annotations<TypeExtension> annotation) {
    if (annotation == Annotations.ON_TYPE_CLASS_CREATION) {
      typeSpec = context.onType(context, typeSpec, objectType, buildPhase);
    }

    typeSpec =
        currentBuild.getTypeExtension(annotation, objectType)
            .onType(context, typeSpec, objectType, buildPhase);
    return typeSpec;
  }
}
