package org.raml.jaxrs.generator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;
import org.raml.jaxrs.generator.builders.JAXBHelper;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.jaxrs.generator.builders.resources.ResourceGenerator;
import org.raml.jaxrs.generator.builders.resources.ResourceInterface;
import org.raml.jaxrs.generator.builders.types.CompositeRamlTypeGenerator;
import org.raml.jaxrs.generator.builders.types.RamlTypeGenerator;
import org.raml.jaxrs.generator.builders.types.RamlTypeGeneratorImplementation;
import org.raml.jaxrs.generator.builders.types.RamlTypeGeneratorInterface;
import org.raml.jaxrs.generator.builders.TypeDescriber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.raml.jaxrs.generator.Paths.relativize;

/**
 * Created by Jean-Philippe Belanger on 10/26/16.
 * The art of building stuff is here.
 * Factory for building root stuff.
 */
public class CurrentBuild {

    private final String defaultPackage;

    private final List<ResourceGenerator> resources = new ArrayList<ResourceGenerator>();
    private final Map<String, TypeGenerator> types = new HashMap<>();

    public CurrentBuild(String defaultPackage) {

        this.defaultPackage = defaultPackage;
    }

    public String getDefaultPackage() {
        return defaultPackage;
    }

    public ResourceGenerator createResource(String name, String relativeURI) {

        ResourceGenerator builder = new ResourceInterface(this, name, relativize(relativeURI));
        resources.add(builder);
        return builder;
    }

    public void generate(String rootDirectory) throws IOException {

        ResponseSupport.buildSupportClasses(rootDirectory, this.defaultPackage);
        for (ResourceGenerator resource : resources) {
            resource.output(rootDirectory);
        }

        for (TypeGenerator b: types.values()) {
            b.output(rootDirectory);
        }
    }

    public RamlTypeGenerator createType(String name, List<String> parentTypes) {

        RamlTypeGeneratorInterface intf = new RamlTypeGeneratorInterface(this, name, parentTypes);
        RamlTypeGeneratorImplementation impl = new RamlTypeGeneratorImplementation(this, name, name);

        CompositeRamlTypeGenerator compositeTypeBuilder = new CompositeRamlTypeGenerator(intf, impl);
        types.put(name, compositeTypeBuilder);
        return compositeTypeBuilder;
    }

    public TypeGenerator getDeclaredType(String parentType) {

        return types.get(parentType);
    }

    public void javaTypeName(String type, TypeDescriber describer) {
        Class<?> scalar = ScalarTypes.scalarToJavaType(type);
        if ( scalar != null ){

            describer.asJavaType(this, scalar);
        } else {

            TypeGenerator builder = types.get(type);
            if ( builder != null ) {

                describer.asBuiltType(this, builder.getGeneratedJavaType());
            } else {

                throw new IllegalArgumentException("unknown type " + type);
            }
        }


    }

    public void createTypeFromJsonSchema(final String name, final String jsonSchema) {


        try {
            GenerationConfig config = new DefaultGenerationConfig() {
                @Override
                public boolean isGenerateBuilders() { // set config option by overriding method
                    return true;
                }
            };

            final SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(), new SchemaStore()),
                    new SchemaGenerator());
            final JCodeModel codeModel = new JCodeModel();

            mapper.generate(codeModel, name, defaultPackage, jsonSchema);

            types.put(name, new JsonSchemaTypeGenerator(mapper, defaultPackage, name, codeModel));
        } catch (IOException e) {
            throw new GenerationException(e);
        }
    }

    public void createTypeFromXmlSchema(final String name, String schema) {

        try {
            File schemaFile = JAXBHelper.saveSchema(schema);
            final JCodeModel codeModel = new JCodeModel();

            Map<String, JClass> generated = JAXBHelper.generateClassesFromXmlSchemas(defaultPackage, schemaFile, codeModel);

            types.put(name, new XmlSchemaTypeGenerator(codeModel, defaultPackage, name));

        } catch (Exception e) {

            throw new GenerationException(e);
        }
    }

}
