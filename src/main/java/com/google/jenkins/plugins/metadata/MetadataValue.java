/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.jenkins.plugins.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * A marker interface for value types intended to be used as metadata contained
 * in {@link MetadataContainer#getMetadata()}.
 *
 * Implementations must make their classes serializable using
 * {@link MetadataContainer#serialize(MetadataValue)} and
 * {@link MetadataContainer#deserialize(Class, String)}. That means they can be
 * handled by Jackson's JSON generator / parser. Most Java beans would suffice,
 * or if immutable POJO, annotate the constructor with
 * {@link com.fasterxml.jackson.core.JsonFactory}. Also, any type used for
 * the member variables need to be serializable by the same library.
 */
@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
public interface MetadataValue {
  /**
   * @return the key of this {@link MetadataValue}, useful when grouping in a
   *         {@link MetadataContainer}.
   */
  @JsonIgnore
  String getKey();
}
