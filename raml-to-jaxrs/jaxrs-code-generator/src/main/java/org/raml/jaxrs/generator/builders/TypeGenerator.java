package org.raml.jaxrs.generator.builders;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

/**
 * Created by Jean-Philippe Belanger on 11/20/16.
 * Just potential zeroes and ones
 */
public interface TypeGenerator<T> extends Generator<T> {

    enum TYPE { INTERFACE, IMPLEMENTATION }
    void output(CodeContainer<T> rootDirectory, TYPE type) throws IOException;

    TypeName getGeneratedJavaType();
}
