package org.raml.jaxrs.generator.extension.resources;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.ramltypes.GMethod;
import org.raml.jaxrs.generator.ramltypes.GResource;
import org.raml.jaxrs.generator.ramltypes.GResponse;
import org.raml.jaxrs.generator.v08.V08GResource;
import org.raml.jaxrs.generator.v08.V08Method;
import org.raml.jaxrs.generator.v08.V08Response;

/**
 * Created by Jean-Philippe Belanger on 1/22/17.
 * Just potential zeroes and ones
 *
 * QUick and dirty
 */
public interface GlobalResourceExtension<M extends GMethod, R extends GResource, S extends GResponse> extends
        ResponseClassExtension<M>,
        ResourceClassExtension<R>,
        ResponseMethodExtension<S>,
        ResourceMethodExtension<M> {

    GlobalResourceExtension<V08Method, V08GResource, V08Response> NULL_EXTENSION = new GlobalResourceExtension<V08Method, V08GResource, V08Response>() {
        @Override
        public TypeSpec.Builder onResource(ResourceContext context, V08GResource resource, TypeSpec.Builder typeSpec) {
            return typeSpec;
        }

        @Override
        public MethodSpec.Builder onMethod(ResourceContext context, V08Method method, MethodSpec.Builder methodSpec) {
            return methodSpec;
        }

        @Override
        public TypeSpec.Builder onMethod(ResourceContext context, V08Method method, TypeSpec.Builder typeSpec) {
            return typeSpec;
        }

        @Override
        public MethodSpec.Builder onMethod(ResourceContext context, V08Response method, MethodSpec.Builder methodSpec) {
            return methodSpec;
        }
    };

}
