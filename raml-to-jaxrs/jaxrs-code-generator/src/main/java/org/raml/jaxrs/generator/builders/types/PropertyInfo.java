package org.raml.jaxrs.generator.builders.types;

import com.squareup.javapoet.TypeName;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.GenerationException;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.Map;

/**
 * Created by Jean-Philippe Belanger on 11/13/16.
 * Just potential zeroes and ones
 */
public class PropertyInfo {

    private final String name;
    private String internalTypeName;
    private  TypeDeclaration type;

    public PropertyInfo(String name, TypeDeclaration type) {
        this.name = name;
        this.type = type;
    }

    public PropertyInfo(String name, String internalTypeName, TypeDeclaration declaration) {
        this.name = name;
        this.internalTypeName = internalTypeName;
        this.type = declaration;
    }

    public String getName() {
        return name;
    }

    public TypeDeclaration getType() {
        return type;
    }

    public TypeName resolve(CurrentBuild currentBuild, Map<String, JavaPoetTypeGenerator> internalTypes) {

        // If the type is not internal, then it must exist.  The type of the property must be correct.
        if ( internalTypeName == null ) {

            return currentBuild.getJavaType(type);
        } else {

            TypeGenerator tg = internalTypes.get(internalTypeName);
            if (  tg == null ) {
                throw new GenerationException("unable to resolve type for " + name);
            } else {

                return tg.getGeneratedJavaType();
            }
        }
    }
}
