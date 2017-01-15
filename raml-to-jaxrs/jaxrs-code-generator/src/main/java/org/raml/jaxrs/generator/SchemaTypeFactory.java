package org.raml.jaxrs.generator;

import com.squareup.javapoet.ClassName;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import org.raml.jaxrs.generator.builders.JAXBHelper;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.jaxrs.generator.ramltypes.GType;

import java.io.File;
import java.util.Map;

/**
 * Created by Jean-Philippe Belanger on 12/2/16.
 * Just potential zeroes and ones
 */
public class SchemaTypeFactory {

    public static TypeGenerator createXmlType(CurrentBuild currentBuild, GType type) {
        File schemaFile = null;
        try {
            schemaFile = JAXBHelper.saveSchema(type.schema());
            final JCodeModel codeModel = new JCodeModel();

            Map<String, JClass> generated = JAXBHelper.generateClassesFromXmlSchemas(currentBuild.getModelPackage(), schemaFile, codeModel);
            XmlSchemaTypeGenerator gen = new XmlSchemaTypeGenerator(codeModel, currentBuild.getModelPackage(),
                    generated.values().iterator().next());
            type.setJavaType(gen.getGeneratedJavaType());
            currentBuild.newGenerator(type.name(), gen);
            return gen;
        } catch (Exception e) {

            throw new GenerationException(e);
        } finally {

            if ( schemaFile != null ) {

                schemaFile.delete();
            }
        }
    }

    public static TypeGenerator createJsonType(CurrentBuild currentBuild, GType type) {

        JsonSchemaTypeGenerator gen = new JsonSchemaTypeGenerator(currentBuild, currentBuild.getModelPackage(),
                (ClassName) type.defaultJavaTypeName(currentBuild.getModelPackage()), type.schema());
        currentBuild.newGenerator(type.name(), gen);
        return gen;
    }


}
