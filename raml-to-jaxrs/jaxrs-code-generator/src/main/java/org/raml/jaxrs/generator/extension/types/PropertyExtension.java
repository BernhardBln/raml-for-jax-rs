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
package org.raml.jaxrs.generator.extension.types;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.builders.BuildPhase;
import org.raml.jaxrs.generator.v10.V10GProperty;
import org.raml.jaxrs.generator.v10.V10GType;

/**
 * Created by Jean-Philippe Belanger on 1/26/17. Just potential zeroes and ones
 */
public interface PropertyExtension {

  /* enough ... ? */
  void onProperty(TypeContext context, TypeSpec.Builder builder, V10GType containingType, V10GProperty property,
                  BuildPhase buildPhase);

  void onProperty(TypeContext context, FieldSpec.Builder builder, V10GType containingType, V10GProperty property,
                  BuildPhase buildPhase);

  void onPropertyGetter(TypeContext context, MethodSpec.Builder builder, V10GType containingType, V10GProperty property,
                        BuildPhase buildPhase);

  void onPropertySetter(TypeContext context, MethodSpec.Builder builder, ParameterSpec.Builder parameter,
                        V10GType containingType, V10GProperty property,
                        BuildPhase buildPhase);
}
