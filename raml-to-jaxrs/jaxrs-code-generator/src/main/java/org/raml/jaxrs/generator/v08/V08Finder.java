package org.raml.jaxrs.generator.v08;

import org.raml.jaxrs.generator.GAbstractionFactory;
import org.raml.jaxrs.generator.GFinder;
import org.raml.jaxrs.generator.GFinderListener;
import org.raml.jaxrs.generator.v10.TypeUtils;
import org.raml.jaxrs.generator.v10.V10GType;
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.api.GlobalSchema;
import org.raml.v2.api.model.v08.bodies.BodyLike;
import org.raml.v2.api.model.v08.bodies.Response;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.resources.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jean-Philippe Belanger on 12/6/16.
 * Just potential zeroes and ones
 */
public class V08Finder implements GFinder {

    private final Api api;
    private final GAbstractionFactory factory;


    public V08Finder(Api api, GAbstractionFactory factory) {
        this.api = api;
        this.factory =  factory;
    }

    @Override
    public GFinder findTypes(GFinderListener listener) {


        goThroughSchemas(api.schemas());

        resourceTypes(api.resources(), listener);

        return this;
    }

    private void goThroughSchemas(List<GlobalSchema> schemas) {

        for (GlobalSchema schema : schemas) {

            V08GType type = new V08GType(schema);
        }
    }

    private void resourceTypes(List<Resource> resources, GFinderListener listener) {

        for (Resource resource : resources) {

            resourceTypes(resource.resources(), listener);
            for (Method method : resource.methods()) {

                typesInBodies(resource, method, method.body(), listener);
            }
        }
    }

    private void typesInBodies(Resource resource, Method method, List<BodyLike> body, GFinderListener listener) {
        for (BodyLike typeDeclaration : body) {

            V08GType type = new V08GType(resource, method, typeDeclaration);
            listener.newTypeDeclaration(type);
        }

        for (Response response : method.responses()) {
            for (BodyLike typeDeclaration : response.body()) {

                V08GType type = new V08GType(resource, method, response, typeDeclaration);
                listener.newTypeDeclaration(type);
            }
        }
    }
}
