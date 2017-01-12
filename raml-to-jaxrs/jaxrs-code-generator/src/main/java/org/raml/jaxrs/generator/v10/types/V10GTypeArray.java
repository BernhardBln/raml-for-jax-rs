package org.raml.jaxrs.generator.v10.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GObjectType;
import org.raml.jaxrs.generator.ramltypes.GType;
import org.raml.jaxrs.generator.v10.V10TypeRegistry;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 1/5/17.
 * Just potential zeroes and ones
 */
public class V10GTypeArray extends V10GTypeHelper {
    private final V10TypeRegistry registry;
    private final String name;
    private final ArrayTypeDeclaration typeDeclaration;

    public V10GTypeArray(V10TypeRegistry registry, String name, ArrayTypeDeclaration typeDeclaration) {
        super(name);
        this.registry = registry;
        this.name = name;
        this.typeDeclaration = typeDeclaration;
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
    public boolean isArray() {
        return true;
    }

    @Override
    public GType arrayContents() {
        return registry.fetchType(typeDeclaration.items().name(), typeDeclaration.items());
    }

    @Override
    public TypeName defaultJavaTypeName(String pack) {
        return ParameterizedTypeName.get(
                ClassName.get(List.class),
                arrayContents().defaultJavaTypeName(pack));
    }

    @Override
    public ClassName javaImplementationName(String pack) {
        return null;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public void construct(CurrentBuild currentBuild, GObjectType objectType) {

    }
}
