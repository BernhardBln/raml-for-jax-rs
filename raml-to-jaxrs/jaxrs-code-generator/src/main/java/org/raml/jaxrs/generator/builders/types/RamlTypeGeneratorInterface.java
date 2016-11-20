package org.raml.jaxrs.generator.builders.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.builders.TypeBuilderHelpers;
import org.raml.jaxrs.generator.builders.TypeGenerator;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jean-Philippe Belanger on 11/13/16.
 * Just potential zeroes and ones
 */
public class RamlTypeGeneratorInterface implements RamlTypeGenerator {
    private final CurrentBuild build;
    private final String name;
    private final List<String> parentTypes;

    private Map<String, PropertyInfo> propertyInfos = new HashMap<>();

    public RamlTypeGeneratorInterface(CurrentBuild build, String name, List<String> parentTypes) {
        this.build = build;
        this.name = name;
        this.parentTypes = parentTypes;
    }

    @Override
    public RamlTypeGenerator addProperty(String type, String name) {

        propertyInfos.put(name, new PropertyInfo(type, name));
        return this;
    }

    @Override
    public void output(String rootDirectory) throws IOException {

        TypeSpec.Builder typeSpec = TypeSpec
                .interfaceBuilder(
                        ClassName.get(build.getDefaultPackage(), Names.buildTypeName(name)))
                    .addModifiers(Modifier.PUBLIC);

        List<TypeGenerator> propsFromParents = new ArrayList<>();
        for (String parentType : parentTypes) {

            TypeGenerator builder = build.getDeclaredType(parentType);
            propsFromParents.add(builder);
            typeSpec.addSuperinterface(ClassName.get(build.getDefaultPackage(), Names.buildTypeName(parentType)));
        }

        for (PropertyInfo propertyInfo : propertyInfos.values()) {

            if ( noParentDeclares(propsFromParents, propertyInfo.getName())) {
                final MethodSpec.Builder getSpec = MethodSpec
                        .methodBuilder("get" + Names.buildTypeName(propertyInfo.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                build.javaTypeName(propertyInfo.getType(), TypeBuilderHelpers.forReturnValue(getSpec));

                MethodSpec.Builder setSpec = MethodSpec
                        .methodBuilder("set" + Names.buildTypeName(propertyInfo.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                build.javaTypeName(propertyInfo.getType(), TypeBuilderHelpers.forParameter(setSpec, propertyInfo.getName()));
                typeSpec.addMethod(getSpec.build());
                typeSpec.addMethod(setSpec.build());
            }
        }

        JavaFile.Builder file = JavaFile.builder(build.getDefaultPackage(), typeSpec.build());
        file.build().writeTo(new File(rootDirectory));
    }

    private boolean noParentDeclares(List<TypeGenerator> propsFromParents, String name) {

        for (TypeGenerator propsFromParent : propsFromParents) {

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

        for (String parentType : parentTypes) {

            TypeGenerator builder = build.getDeclaredType(parentType);
            if (builder.declaresProperty(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getGeneratedJavaType() {

        return build.getDefaultPackage() + "." + Names.buildTypeName(name);
    }
}
