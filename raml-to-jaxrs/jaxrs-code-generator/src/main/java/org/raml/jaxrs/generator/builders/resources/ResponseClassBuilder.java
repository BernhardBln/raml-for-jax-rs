package org.raml.jaxrs.generator.builders.resources;

import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.builders.Generator;

/**
 * Created by Jean-Philippe Belanger on 11/5/16.
 * Just potential zeroes and ones
 */
public interface ResponseClassBuilder extends Generator<TypeSpec.Builder>{
    String name();

    void withResponse(String value);
    void withResponse(String code, String name, String type);
}