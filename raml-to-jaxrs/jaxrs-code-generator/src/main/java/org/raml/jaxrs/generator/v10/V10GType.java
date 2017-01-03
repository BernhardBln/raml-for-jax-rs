package org.raml.jaxrs.generator.v10;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GObjectType;
import org.raml.jaxrs.generator.GProperty;
import org.raml.jaxrs.generator.GType;
import org.raml.jaxrs.generator.GenerationException;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.SchemaTypeFactory;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.JSONTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.XMLTypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 12/10/16.
 * Just potential zeroes and ones
 */
public class V10GType implements GType {

    private final V10TypeRegistry registry;
    private final TypeDeclaration typeDeclaration;
    private final String name;
    private final String defaultJavatypeName;
    private final boolean inline;
    private final List<GProperty> properties;
    private final List<GType> parentTypes;


    static V10GType createRequestBodyType(V10TypeRegistry registry, Resource resource, Method method,
            TypeDeclaration typeDeclaration) {

        return new V10GType(
                registry,
                typeDeclaration,
                Names.ramlTypeName(resource, method, typeDeclaration),
                Annotations.CLASS_NAME.get(typeDeclaration, Names.javaTypeName(resource, method, typeDeclaration)),
                true,
                getProperties(typeDeclaration, registry),
                getParents(typeDeclaration, registry));
    }

    private static List<GType> getParents(TypeDeclaration typeDeclaration, final V10TypeRegistry registry) {
        return Lists.transform(typeDeclaration.parentTypes(), new Function<TypeDeclaration, GType>() {
            @Nullable
            @Override
            public GType apply(@Nullable TypeDeclaration input) {
                return registry.fetchType(input);
            }
        });
    }

    static V10GType createResponseBodyType(V10TypeRegistry registry, Resource resource, Method method,
            Response response,
            TypeDeclaration typeDeclaration) {
        return new V10GType(
                registry,
                typeDeclaration,
                Names.ramlTypeName(resource, method, response, typeDeclaration),
                Annotations.CLASS_NAME.get(typeDeclaration, Names.javaTypeName(resource, method, response, typeDeclaration)),
                true,
                getProperties(typeDeclaration, registry),
                getParents(typeDeclaration, registry));
    }

    static V10GType createExplicitlyNamedType(V10TypeRegistry registry, String s, TypeDeclaration typeDeclaration) {
        return new V10GType(
                registry,
                typeDeclaration,
                s,
                Annotations.CLASS_NAME.get(typeDeclaration, Names.typeName(typeDeclaration.name())),
                false,
                getProperties(typeDeclaration, registry),
                getParents(typeDeclaration, registry));
    }

    static V10GType createExplicitlyNamedType(V10TypeRegistry registry, String ramlName, String javaClassName,
            TypeDeclaration typeDeclaration) {
        return new V10GType(
                registry,
                typeDeclaration,
                ramlName,
                Annotations.CLASS_NAME.get(typeDeclaration, javaClassName),
                false,
                getProperties(typeDeclaration, registry),
                getParents(typeDeclaration, registry));
    }


    private V10GType(V10TypeRegistry registry, TypeDeclaration typeDeclaration, String realName, String defaultJavatypeName, boolean inline, List<GProperty> properties, List<GType> parentTypes) {
        this.registry = registry;
        this.typeDeclaration = typeDeclaration;
        this.name = realName;
        this.defaultJavatypeName = defaultJavatypeName;
        this.inline = inline;
        this.properties = properties;
        this.parentTypes = parentTypes;
    }

    private static List<GProperty> getProperties(final TypeDeclaration input, final V10TypeRegistry registry) {

        if ( input instanceof ObjectTypeDeclaration) {

            ObjectTypeDeclaration otd = (ObjectTypeDeclaration) input;
            return Lists.transform(otd.properties(), new Function<TypeDeclaration, GProperty>() {
                @Nullable
                @Override
                public GProperty apply(@Nullable TypeDeclaration declaration) {

                    return new V10GProperty(declaration, createExplicitlyNamedType(registry, declaration.type(), declaration));
                }
            });
        } else {

            return Collections.emptyList();
        }
    }

    @Override
    public TypeDeclaration implementation() {
        return typeDeclaration;
    }

    @Override
    public String type() {
        return typeDeclaration.type();
    }

    @Override
    public String name() {

        return name;
    }

    @Override
    public boolean isJson() {

        return typeDeclaration instanceof JSONTypeDeclaration;
    }

    @Override
    public boolean isUnion() {

        return typeDeclaration instanceof UnionTypeDeclaration;
    }

    @Override
    public boolean isXml() {
        return typeDeclaration instanceof XMLTypeDeclaration;
    }

    @Override
    public boolean isObject() {
        return typeDeclaration instanceof ObjectTypeDeclaration;
    }

    @Override
    public String schema() {
        if ( typeDeclaration instanceof XMLTypeDeclaration ) {
            return ((XMLTypeDeclaration) typeDeclaration).schemaContent();
        }

        if ( typeDeclaration instanceof JSONTypeDeclaration) {

            return ((JSONTypeDeclaration) typeDeclaration).schemaContent();
        }

        throw new GenerationException("type " + this + " has no schema");
    }

    @Override
    public List<GType> parentTypes() {
        return parentTypes;
    }

    @Override
    public List<GProperty> properties() {

        return properties;
    }


    @Override
    public boolean isArray() {
        return typeDeclaration instanceof ArrayTypeDeclaration;
    }

    @Override
    public GType arrayContents() {

        ArrayTypeDeclaration d = (ArrayTypeDeclaration) typeDeclaration;
        return createExplicitlyNamedType(registry, d.items().name().replaceAll("\\[\\]", ""),  d.items());
    }

    @Override
    public String defaultJavaTypeName() {

        return defaultJavatypeName;
    }

    public String javaImplementationName() {

        return Annotations.IMPLEMENTATION_CLASS_NAME.get(typeDeclaration, defaultJavatypeName + "Impl");
    }

    @Override
    public boolean isEnum() {
        return  typeDeclaration instanceof StringTypeDeclaration && ((StringTypeDeclaration) typeDeclaration).enumValues().size() > 0;
    }

    @Override
    public List<String> enumValues() {
        return ((StringTypeDeclaration)typeDeclaration).enumValues();
    }

    @Override
    public boolean isInline() {
        return inline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        V10GType v10GType = (V10GType) o;

        return name.equals(v10GType.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "V10GType{" +
                "input=" + typeDeclaration.name() + ":" + typeDeclaration.type()+
                ", name='" + name() + '\'' +
                '}';
    }

    @Override
    public void construct(final CurrentBuild currentBuild, GObjectType objectType) {
        objectType.dispatch(new GObjectType.GObjectTypeDispatcher() {

            @Override
            public void onPlainObject() {

                V10TypeFactory.createObjectType(registry, currentBuild, V10GType.this, true);
            }

            @Override
            public void onXmlObject() {

                SchemaTypeFactory.createXmlType(currentBuild, V10GType.this);
            }

            @Override
            public void onJsonObject() {

                SchemaTypeFactory.createJsonType(currentBuild, V10GType.this);
            }

            @Override
            public void onEnumeration() {

                V10TypeFactory.createEnumerationType(currentBuild, V10GType.this);
            }

            @Override
            public void onUnion() {
                V10TypeFactory.createUnion(currentBuild, V10GType.this);
            }
        });
    }


}
