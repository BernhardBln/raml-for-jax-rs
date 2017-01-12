package org.raml.jaxrs.generator.v10;

import org.raml.jaxrs.generator.ramltypes.GProperty;
import org.raml.jaxrs.generator.ramltypes.GType;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

/**
 * Created by Jean-Philippe Belanger on 12/10/16.
 * Just potential zeroes and ones
 */
public class V10GProperty implements GProperty {
    private final TypeDeclaration input;
    private final GType type;

    public V10GProperty(TypeDeclaration input, GType type) {
        this.input = input;
        this.type = type;
    }

    @Override
    public TypeDeclaration implementation() {
        return input;
    }

    @Override
    public String name() {
        return input.name();
    }

    @Override
    public GType type() {
        return type;
    }

    @Override
    public boolean isInline() {
        return TypeUtils.shouldCreateNewClass(input, input.parentTypes().toArray(new TypeDeclaration[0]));
    }

    @Override
    public GProperty overrideType(GType type) {

        return new V10GProperty(input, type);
    }

    @Override
    public String toString() {
        return "V10GProperty{" +
                "name=" + input.name() +
                ", type=" + type +
                '}';
    }
}
