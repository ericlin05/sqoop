/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sqoop.orm;

import junit.framework.TestCase;

public class TestTime extends TestCase {

  public void testConstructor1() {
    Time t = new Time(12, 13, 14, 1234);

    assertEquals(12, t.getHours());
    assertEquals(13, t.getMinutes());
    assertEquals(14, t.getSeconds());
    assertEquals(1234, t.getNanos());
  }

  public void testValueOf1() {
    Time t = Time.valueOf("12:13:14.123400");

    assertEquals(12, t.getHours());
    assertEquals(13, t.getMinutes());
    assertEquals(14, t.getSeconds());
    assertEquals(123400000, t.getNanos());
  }

  public void testValueOf2() {
    Time t = Time.valueOf("12:13:14");

    assertEquals(12, t.getHours());
    assertEquals(13, t.getMinutes());
    assertEquals(14, t.getSeconds());
    assertEquals(0, t.getNanos());
  }

  public void testValueOfNanosTooLongCappedAt9() {
    Time t = Time.valueOf("12:13:14.12345678910");

    assertEquals(12, t.getHours());
    assertEquals(13, t.getMinutes());
    assertEquals(14, t.getSeconds());
    assertEquals(123456789, t.getNanos());
  }

  public void testToString1() {
    Time t = new Time(12, 13, 14, 1234);
    assertEquals("12:13:14.000001234", t.toString());
  }

  public void testToString2() {
    Time t = Time.valueOf("12:13:14.1234");
    assertEquals("12:13:14.1234", t.toString());
  }

  public void testToString3() {
    Time t = Time.valueOf("12:13:14");
    assertEquals("12:13:14", t.toString());
  }

  public void testToString4() {
    Time t = Time.valueOf("12:13:14.0");
    assertEquals("12:13:14", t.toString());
  }

  public void testToString5() {
    Time t = Time.valueOf("12:13:14.1000");
    assertEquals("12:13:14.1000", t.toString());
  }

  public void testToString6() {
    Time t = Time.valueOf("12:13:14.000123");
    assertEquals("12:13:14.000123", t.toString());
  }
}
