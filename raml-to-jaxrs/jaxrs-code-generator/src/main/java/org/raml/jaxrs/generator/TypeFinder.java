package org.raml.jaxrs.generator;

/**
 * Created by Jean-Philippe Belanger on 12/8/16.
 * Just potential zeroes and ones
 */
public interface TypeFinder<T extends GeneratorContext> {
    TypeFinder findTypes(TypeFinderListener<T> listener);
}
