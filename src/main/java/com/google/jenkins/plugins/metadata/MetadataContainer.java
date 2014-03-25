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

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import hudson.model.AbstractBuild;
import hudson.model.InvisibleAction;

/**
 * A build's {@link InvisibleAction} containing metadata.
 *
 * This is intended as append-only store for metadata information. Metadata
 * providing code simply call {@link #add(MetadataValue)} to contribute
 * metadata, and {@link #getMetadata()} to consume metadata.
  */
public class MetadataContainer extends InvisibleAction {

  private final Multimap<String, MetadataValue> metadata;

  private /* final */ transient Supplier<ObjectMapper> objectMapper;

  public MetadataContainer() {
    this.metadata = ArrayListMultimap.create();
    readResolve();
  }

  private Object readResolve() {
    objectMapper = Suppliers.memoize(new Supplier<ObjectMapper>() {
      @Override
      public ObjectMapper get() {
        return new ObjectMapper();
      }
    });
    return this;
  }

  /**
   * @return the metadata contained.
   */
  public Multimap<String, MetadataValue> getMetadata() {
    return Multimaps.unmodifiableMultimap(metadata);
  }

  /**
   * Add a {@link MetadataValue} to this container.
   * @param mv the given {@link MetadataValue} to add.
   * @return this {@link MetadataContainer}.
   */
  public MetadataContainer add(MetadataValue mv) {
    metadata.put(mv.getKey(), mv);
    return this;
  }

  /**
   * {@code add} a list of elements
   */
  public <T extends MetadataValue> MetadataContainer addAll(Iterable<T> mvs) {
    for (T mv : mvs) {
      add(mv);
    }
    return this;
  }

  /**
   * {@code remove} a list of elements
   */
  public void removeAll(String key) {
    metadata.removeAll(key);
  }

  /**
   * @return the serialized form of the metadata;
   */
  public Map<String, String> getSerializedMetadata() {
    return Maps.newHashMap(Maps.transformValues(
        metadata.asMap(), new Function<Collection<MetadataValue>, String>() {
          @Override
          public String apply(Collection<MetadataValue> values) {
            return listSerialize(values);
          }
        }));
  }

  /**
   * @param build
   *          a given build.
   * @return the {@link MetadataContainer} for the given build.
   */
  public static synchronized MetadataContainer of(AbstractBuild<?, ?> build) {
    MetadataContainer container = build.getAction(MetadataContainer.class);
    if (container == null) {
      container = new MetadataContainer();
      build.addAction(container);
    }
    return container;
  }

  /**
   * @param metadataValue
   *          the metadata value to be serialized.
   * @return serialized form of the given {@link MetadataValue}.
   * @throws MetadataSerializationException
   *           when serialization runs into problem.
   */
  public String serialize(MetadataValue metadataValue)
      throws MetadataSerializationException {
    return listSerialize(ImmutableList.of(metadataValue));
  }

  /**
   * @param values
   *          the metadata values to be serialized.
   * @return serialized form of the given {@link MetadataValue}.
   * @throws MetadataSerializationException
   *           when serialization runs into problem.
   */
  public <T extends MetadataValue> String listSerialize(Iterable<T> values) {
    try {
      return getObjectMapper()
          .writerWithType(new TypeReference<Iterable<MetadataValue>>() {})
          .writeValueAsString(values);
    } catch (JsonProcessingException ex) {
      throw new MetadataSerializationException(ex);
    }
  }

  /**
   * @param clazz class of the {@link MetadataValue} object to be deserialized.
   * @param serialized the string to deserialize.
   * @return the deserialized {@link MetadataValue} object.
   */
  public <T extends MetadataValue> T deserialize(Class<T> clazz,
      String serialized) {
    return Iterables.getOnlyElement(listDeserialize(clazz, serialized));
  }

  /**
   * @param clazz the class of the {@link MetadataValue} to be deserialize.
   * @param serialized the serialized form to be deserialized.
   * @return a list of deserialized objects.
   */
  public <T extends MetadataValue> Iterable<T> listDeserialize(Class<T> clazz,
      String serialized) {
    try {
      return getObjectMapper().readValue(
          serialized, new TypeReference<Iterable<MetadataValue>>() {});
    } catch (Exception ex) {
      throw new MetadataSerializationException(ex);
    }
  }

  /**
   * @return an {@link ObjectMapper} used for {@link #serialize(MetadataValue)}
   *         and {@link #deserialize(Class, String)}.
   */
  private ObjectMapper getObjectMapper() {
    return objectMapper.get();
  }

  /**
   * @param build a given {@link AbstractBuild}.
   * @return the metadata contained in the {@link MetadataContainer} of the
   *         given {@link AbstractBuild}.
   */
  public static Multimap<String, MetadataValue>
      getMetadata(AbstractBuild<?, ?> build) {
    return of(build).getMetadata();
  }
}
