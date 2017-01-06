package org.raml.jaxrs.generator.v10.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GObjectType;
import org.raml.jaxrs.generator.GType;
import org.raml.jaxrs.generator.v10.V10GProperty;
import org.raml.jaxrs.generator.v10.V10GType;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jean-Philippe Belanger on 1/5/17.
 * Just potential zeroes and ones
 */
public abstract class V10GTypeHelper implements V10GType {


    private String name;

    public V10GTypeHelper(String name) {
        this.name = name;
    }

    @Override
    public boolean isJson() {
        return false;
    }

    @Override
    public boolean isUnion() {
        return false;
    }

    @Override
    public boolean isXml() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public String schema() {
        return null;
    }

    @Override
    public List<V10GType> parentTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<V10GProperty> properties() {
        return Collections.emptyList();
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public GType arrayContents() {
        return null;
    }


    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public List<String> enumValues() {
        return Collections.emptyList();
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public Collection<V10GType> childClasses(String typeName) {
        return Collections.emptyList();
    }

    @Override
    public void construct(CurrentBuild currentBuild, GObjectType objectType) {

    }

    @Override
    public void setJavaType(TypeName generatedJavaType) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if ( ! (o instanceof V10GType) ) {

            return false;
        }

        V10GType v10GType = (V10GType) o;

        return name.equals(v10GType.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
