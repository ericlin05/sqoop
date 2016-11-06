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

import java.sql.Timestamp;

/**
 * This class overwrites the standard java.sql.Time class which does not
 * support nano seconds.
 *
 * This class is created specifically for JIRA: SQOOP-3039 where Sqoop failed
 * with invalid format exception when exporting data from HDFS in format
 * "hh:mm:ss.fffffffff" into RDBMS, because java.sql.Time only supports
 * "hh:mm:ss" format.
 */
public class Time extends java.sql.Time {

  private int nanos;

  /**
   * Constructs a <code>Time</code> object initialized with the given values.
   *
   * @param hour 0 to 23
   * @param minute 0 to 59
   * @param second 0 to 59
   * @param nano 0 to 999,999,999
   * @deprecated instead use the constructor <code>Time(long millis)</code>
   * @exception IllegalArgumentException if the nano argument is out of bounds
   */
  public Time(int hour, int minute, int second, int nano) {
    super(hour, minute, second);
    nanos = nano;
  }

  /**
   * Constructs a <code>Time</code> object using a milliseconds time value.
   *
   * @param time milliseconds since January 1, 1970, 00:00:00 GMT;
   *             a negative number is milliseconds before
   *               January 1, 1970, 00:00:00 GMT
   */
  public Time(long time) {
    super(time);
  }

  /**
   * Converts a string in JDBC time escape format to a <code>Time</code> value.
   *
   * @param s time in format "hh:mm:ss.fffffffff"
   * @return a corresponding <code>Time</code> object
   */
  public static Time valueOf(String s) {

    String[] split = s.split("\\.");
    if(split.length == 2 && split[1].length() > 9) {
      s = split[0] + "." + split[1].substring(0, 9);
    }

    Timestamp t = Timestamp.valueOf("1970-01-01 " + s);
    return new Time(t.getHours(), t.getMinutes(), t.getSeconds(), t.getNanos());
  }

  /**
   * Formats a Time in JDBC Time escape format.
   *         <code>hh:mm:ss.fffffffff</code>,
   * where <code>fffffffff</code> indicates nanoseconds.
   * <P>
   * @return a <code>String</code> object in
   *           <code>hh:mm:ss.fffffffff</code> format
   */
  @Override
  public String toString () {
    Timestamp ts = new Timestamp(
        70, 0, 1,
        this.getHours(), this.getMinutes(), this.getSeconds(),
        this.nanos
    );

    String[] split = ts.toString().split(" ");
    if(split.length == 2) {
      return split[1];
    }

    // VERY unlikely to hit here (unless a but in Timestamp class), but just in case
    java.sql.Time t = new java.sql.Time(
        this.getHours(),
        this.getMinutes(),
        this.getSeconds()
    );
    return t.toString() + "." + this.nanos;
  }

  /**
   * Returns the value of Nano seconds
   *
   * @return int
   */
  public int getNanos() {
    return nanos;
  }
}
