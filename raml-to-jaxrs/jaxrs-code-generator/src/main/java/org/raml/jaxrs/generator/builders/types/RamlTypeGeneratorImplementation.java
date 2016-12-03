package org.raml.jaxrs.generator.builders.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.builders.AbstractTypeGenerator;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.raml.jaxrs.generator.builders.TypeBuilderHelpers.forField;
import static org.raml.jaxrs.generator.builders.TypeBuilderHelpers.forParameter;
import static org.raml.jaxrs.generator.builders.TypeBuilderHelpers.forReturnValue;

/**
 * Created by Jean-Philippe Belanger on 11/13/16.
 * Just potential zeroes and ones
 */
public class RamlTypeGeneratorImplementation extends AbstractTypeGenerator<TypeSpec.Builder> implements RamlTypeGenerator {
    private final CurrentBuild build;
    private final ClassName className;
    private final ClassName parentClassName;
    private final List<TypeDeclaration> parentTypes;
    private Map<String, JavaPoetTypeGenerator> internalTypes = new HashMap<>();

    private Map<String, PropertyInfo> propertyInfos = new HashMap<>();
    private final TypeDeclaration typeDeclaration;


    public RamlTypeGeneratorImplementation(CurrentBuild build, ClassName className, ClassName parentClassName,
            List<TypeDeclaration> parentTypes,
            List<PropertyInfo> properties, Map<String, JavaPoetTypeGenerator> internalTypes, TypeDeclaration typeDeclaration) {

        this.build = build;
        this.className = className;
        this.parentClassName = parentClassName;
        this.parentTypes = parentTypes;
        this.internalTypes = internalTypes;
        this.typeDeclaration = typeDeclaration;
        for (PropertyInfo property : properties) {
            propertyInfos.put(property.getName(), property);
        }

    }


    @Override
    public void output(CodeContainer<TypeSpec.Builder> container, TYPE type) throws IOException {


        final TypeSpec.Builder typeSpec = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC);

        build.withTypeListeners().onTypeImplementation(typeSpec);

        typeSpec.addSuperinterface(parentClassName);


        for (TypeGenerator internalType : internalTypes.values()) {

            internalType.output(new CodeContainer<TypeSpec.Builder>() {
                @Override
                public void into(TypeSpec.Builder g) throws IOException {

                    g.addModifiers(Modifier.STATIC);
                    typeSpec.addType(g.build());
                }
            }, type);
        }

        for (PropertyInfo propertyInfo : propertyInfos.values()) {

            typeSpec.addField(FieldSpec.builder(build.getJavaType(propertyInfo.getType(), internalTypes), propertyInfo.getName()).addModifiers(Modifier.PRIVATE).build());

            final MethodSpec.Builder getSpec = MethodSpec
                    .methodBuilder("get" + Names.buildTypeName(propertyInfo.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this." + propertyInfo.getName());

            getSpec.returns(build.getJavaType(propertyInfo.getType(), internalTypes));

            MethodSpec.Builder setSpec = MethodSpec
                    .methodBuilder("set" + Names.buildTypeName(propertyInfo.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this." + propertyInfo.getName() + " = " + propertyInfo.getName());

                setSpec.addParameter(ParameterSpec.builder(build.getJavaType(propertyInfo.getType(), internalTypes), propertyInfo.getName()).build());
            typeSpec.addMethod(getSpec.build());
            typeSpec.addMethod(setSpec.build());
        }

        // JavaFile.Builder file = JavaFile.builder(build.getDefaultPackage());
        container.into(typeSpec);

        //file.build().writeTo(new File(rootDirectory));
    }

    @Override
    public boolean declaresProperty(String name) {

        return true;
    }

    @Override
    public TypeName getGeneratedJavaType() {
        return null;
    }
}
