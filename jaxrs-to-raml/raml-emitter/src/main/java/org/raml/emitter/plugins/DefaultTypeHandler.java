/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.emitter.plugins;

import org.raml.api.RamlMediaType;
import org.raml.api.RamlResourceMethod;
import org.raml.api.ScalarType;
import org.raml.emitter.types.TypeRegistry;
import org.raml.utilities.IndentedAppendable;

import java.io.IOException;
import java.lang.reflect.Type;

import static java.lang.String.format;

/**
 * Created by Jean-Philippe Belanger on 3/26/17. Just potential zeroes and ones
 */
public class DefaultTypeHandler implements TypeHandler {

  @Override
  public boolean handlesType(RamlResourceMethod method, Type type) {
    return true;
  }

  @Override
  public void writeType(TypeRegistry registry, IndentedAppendable writer, RamlMediaType ramlMediaType,
                        RamlResourceMethod method, Type bodyType)
      throws IOException {

    writer.appendLine(format("%s:", ramlMediaType.toStringRepresentation()));
    if (ScalarType.fromType(bodyType).isPresent()) {

      writer.indent();
      writer.appendLine("type: " + ScalarType.fromType(bodyType).get().getRamlSyntax());
      writer.outdent();
    } else {

      throw new IOException(bodyType + " is not a primitive type");
    }


    writer.outdent();

  }
}
