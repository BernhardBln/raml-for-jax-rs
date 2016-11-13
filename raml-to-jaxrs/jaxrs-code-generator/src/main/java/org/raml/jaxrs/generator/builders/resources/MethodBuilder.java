package org.raml.jaxrs.generator.builders.resources;

/**
 * Created by Jean-Philippe Belanger on 10/30/16.
 * Just potential zeroes and ones
 */
public interface MethodBuilder {
    MethodBuilder addQueryParameter(String name, String type);
    MethodBuilder addPathParameter(String name, String type);
    MethodBuilder addEntityParameter(String name, String type);
    MethodBuilder addConsumeAnnotation(String type);
    MethodBuilder addPathAnnotation(String path);

    void output();

}
