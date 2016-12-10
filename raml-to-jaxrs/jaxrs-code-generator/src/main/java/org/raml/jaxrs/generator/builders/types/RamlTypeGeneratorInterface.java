package org.raml.jaxrs.generator.builders.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GeneratorType;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.builders.AbstractTypeGenerator;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.Generator;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jean-Philippe Belanger on 11/13/16.
 * Just potential zeroes and ones
 */
public class RamlTypeGeneratorInterface extends AbstractTypeGenerator<TypeSpec.Builder> implements RamlTypeGenerator {
    private final CurrentBuild build;
    private final List<TypeDeclaration> parentTypes;
    private final ClassName interf;

    private Map<String, PropertyInfo> propertyInfos = new HashMap<>();
    private Map<String, JavaPoetTypeGenerator> internalTypes = new HashMap<>();
    private final TypeDeclaration typeDeclaration;


    public RamlTypeGeneratorInterface(CurrentBuild currentBuild, ClassName interf, List<TypeDeclaration> parentTypes,
            List<PropertyInfo> properties, Map<String, JavaPoetTypeGenerator> internalTypes, TypeDeclaration typeDeclaration) {

        this.build = currentBuild;
        this.interf = interf;
        this.parentTypes = parentTypes;
        this.internalTypes = internalTypes;
        this.typeDeclaration = typeDeclaration;
        for (PropertyInfo property : properties) {
            propertyInfos.put(property.getName(), property);
        }
    }

    @Override
    public void output(CodeContainer<TypeSpec.Builder> into, TYPE type) throws IOException {

        final TypeSpec.Builder typeSpec = TypeSpec
                .interfaceBuilder(interf)
                .addModifiers(Modifier.PUBLIC);

        build.withTypeListeners().onTypeDeclaration(typeSpec, typeDeclaration);

        for (JavaPoetTypeGenerator internalType : internalTypes.values()) {

            internalType.output(new CodeContainer<TypeSpec.Builder>() {
                @Override
                public void into(TypeSpec.Builder g) throws IOException {

                    g.addModifiers(Modifier.STATIC);
                    typeSpec.addType(g.build());
                }
            }, type);
        }


        List<GeneratorType<?>> propsFromParents = new ArrayList<>();
        for (TypeDeclaration parentType : parentTypes) {

            if ( parentType.name().equals("object") ) {

                continue;
            }
            GeneratorType<?> builder = build.getDeclaredType(parentType.name());

            propsFromParents.add(builder);
            typeSpec.addSuperinterface(ClassName.get(build.getModelPackage(), builder.getJavaTypeName()));
        }

        for (PropertyInfo propertyInfo : propertyInfos.values()) {

            if (noParentDeclares(propsFromParents, propertyInfo.getName())) {
                final MethodSpec.Builder getSpec = MethodSpec
                        .methodBuilder("get" + Names.typeName(propertyInfo.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                getSpec.returns(propertyInfo.resolve(build, internalTypes));
                build.withTypeListeners().onGetterMethodDeclaration(getSpec, propertyInfo.getType());

                MethodSpec.Builder setSpec = MethodSpec
                        .methodBuilder("set" + Names.typeName(propertyInfo.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                ParameterSpec.Builder parameterSpec = ParameterSpec
                        .builder(propertyInfo.resolve(build, internalTypes), Names.variableName(propertyInfo.getName()));
                build.withTypeListeners().onSetterMethodImplementation(setSpec, parameterSpec, propertyInfo.getType() );
                setSpec.addParameter(
                        parameterSpec.build());

                typeSpec.addMethod(getSpec.build());
                typeSpec.addMethod(setSpec.build());
            }
        }

        into.into(typeSpec);
    }

    private boolean noParentDeclares(List<GeneratorType<?>> propsFromParents, String name) {

        for (GeneratorType<?> propsFromParent : propsFromParents) {

            if (propsFromParent.declaresProperty(name)) {

                return false;
            }

        }

        return true;
    }

    @Override
    public boolean declaresProperty(String name) {

        if (propertyInfos.containsKey(name)) {
            return true;
        }

        for (TypeDeclaration parentType : parentTypes) {

            GeneratorType<?> builder = build.getDeclaredType(parentType.name());
            if (builder.declaresProperty(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TypeName getGeneratedJavaType() {

        return interf;
    }
}
