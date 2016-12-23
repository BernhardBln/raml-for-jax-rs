package org.raml.jaxrs.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.builders.AbstractTypeGenerator;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.types.RamlTypeGenerator;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import javax.lang.model.element.Modifier;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 12/22/16.
 * Just potential zeroes and ones
 */
public class EnumerationGenerator  extends AbstractTypeGenerator<TypeSpec.Builder> implements RamlTypeGenerator {

    private final CurrentBuild build;
    private final ClassName javaName;
    private final List<String> values;
    private TypeDeclaration typeDeclaration;

    public EnumerationGenerator(CurrentBuild build, TypeDeclaration typeDeclaration, ClassName javaName, List<String> values) {
        this.build = build;
        this.typeDeclaration = typeDeclaration;
        this.javaName = javaName;
        this.values = values;
    }


    @Override
    public void output(CodeContainer<TypeSpec.Builder> rootDirectory, TYPE type) throws IOException {

        FieldSpec.Builder field = FieldSpec.builder(ClassName.get(String.class), "name").addModifiers(Modifier.PRIVATE);
        build.withTypeListeners().onEnumField(field, typeDeclaration);

        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(javaName)
                .addField(field.build())
                .addModifiers(Modifier.PUBLIC)
                .addMethod(
                        MethodSpec.constructorBuilder().addParameter(ClassName.get(String.class), "name")
                                .addStatement("this.$N = $N", "name", "name")
                                .build()
                );
        build.withTypeListeners().onEnumerationClass(enumBuilder, typeDeclaration);

        for (String value : values) {
            TypeSpec.Builder builder = TypeSpec.anonymousClassBuilder("$S", value);
            build.withTypeListeners().onEnumConstant(builder, typeDeclaration, value);

            enumBuilder.addEnumConstant(Names.constantName(value),
                    builder.build());
        }

        rootDirectory.into(enumBuilder);
    }

    @Override
    public TypeName getGeneratedJavaType() {
        return javaName;
    }
}
