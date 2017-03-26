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
package org.raml.emitter.types;

import org.raml.utilities.IndentedAppendable;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by Jean-Philippe Belanger on 3/26/17. Just potential zeroes and ones
 */
public class RamlProperty {

  private RamlType ramlType;
  private String name;

  public RamlProperty(String name, RamlType ramlType) {
    this.ramlType = ramlType;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static RamlProperty createProperty(String name, RamlType ramlType) {
    return new RamlProperty(name, ramlType);
  }

  public void write(IndentedAppendable writer) throws IOException {

    writer.appendLine(name + ": ");
    writer.indent();
    writer.appendLine("type: " + ramlType.getTypeName());
    writer.outdent();
  }
}
