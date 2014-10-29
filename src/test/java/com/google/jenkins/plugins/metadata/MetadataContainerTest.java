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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

import hudson.model.Run;

/**
 * Unit test for {@link MetadataContainer}.
 */
public class MetadataContainerTest {
  @Mock private Run<?, ?> build;
  private MetadataContainer underTest;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    underTest = new MetadataContainer();
  }

  @Test
  public void ofNotPresent() {
    assertNotSame(underTest, MetadataContainer.of(build));
  }

  @Test
  public void ofPresent() {
    when(build.getAction(MetadataContainer.class)).thenReturn(underTest);
    assertSame(underTest, MetadataContainer.of(build));
  }

  @Test
  public void getMetadata() {
    TestMetadata data = new TestMetadata("data");
    underTest.add(data);

    when(build.getAction(MetadataContainer.class)).thenReturn(underTest);
    assertEquals(ImmutableMultimap.of(data.getKey(), data),
        MetadataContainer.getMetadata(build));
  }

  @Test
  public void add() {
    TestMetadata data = new TestMetadata("data");
    underTest.add(data);
    assertThat(underTest.getMetadata().get(data.getKey()),
        contains((MetadataValue) data));
  }

  @Test
  public void addAll() {
    TestMetadata data1 = new TestMetadata("key1");
    TestMetadata data2 = new TestMetadata("key2");
    TestMetadata data3 = new TestMetadata("key2"); // intentionally the same

    underTest.addAll(ImmutableList.of(data1, data2, data3));

    assertEquals(ImmutableList.of(data1),
        underTest.getMetadata().get(data1.getKey()));
    assertEquals(ImmutableList.of(data2, data3),
        underTest.getMetadata().get(data2.getKey()));
  }

  @Test
  public void removeAll() {
    TestMetadata data1 = new TestMetadata("key");
    TestMetadata data2 = new TestMetadata("key");

    underTest.addAll(ImmutableList.of(data1, data2));
    assertEquals(ImmutableList.of(data1, data2),
        underTest.getMetadata().get(data1.getKey()));

    underTest.removeAll(data1.getKey());
    assertTrue(underTest.getMetadata().get(data1.getKey()).isEmpty());
  }

  @Test
  public void getSerializedMetadata() {
    TestMetadata data = new TestMetadata("data");
    underTest.add(data);
    assertEquals(
        underTest.listSerialize(ImmutableList.of(data)),
        underTest.getSerializedMetadata().get(data.getKey()));
  }

  @Test(expected = MetadataSerializationException.class)
  public void serialize_exception() {
    underTest.serialize(new NotSerializable());
  }

  @Test(expected = MetadataSerializationException.class)
  public void deserialize_exception() {
    underTest.deserialize(NotSerializable.class, "bad");
  }

  /**
   * Test metadata class.
   */
  public static class TestMetadata implements MetadataValue {
    public TestMetadata(String key) {
      this.key = key;
    }

    @Override
    public String getKey() {
      return key;
    }
    private final String key;
  }
  /**
   * A class that can't be serialized nor deserialized, for testing of exception
   * handling.
   */
  public static class NotSerializable implements MetadataValue {

    public String getBadField() {
      throw new RuntimeException();
    }
    public void setBadFile(String string) {
      throw new RuntimeException();
    }
    @Override
    public String getKey() {
      return "MY_KEY";
    }
  }
}
