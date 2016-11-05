package org.apache.sqoop.lib;

import java.sql.Timestamp;

/**
 * Created by ericlin on 5/11/16.
 */
public class Time extends java.sql.Time {

  private int nanos;

  /**
   * Constructs a <code>Time</code> object initialized
   * with the given values.
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
      s = split[0] + "." + split[1].substring(0, 8);
    }

    Timestamp t = Timestamp.valueOf("1970-01-01 " + s);
    return new Time(t.getHours(), t.getMinutes(), t.getSeconds(), t.getNanos());
  }

  /**
   * Formats a timestamp in JDBC timestamp escape format.
   *         <code>hh:mm:ss.fffffffff</code>,
   * where <code>fffffffff</code> indicates nanoseconds.
   * <P>
   * @return a <code>String</code> object in
   *           <code>hh:mm:ss.fffffffff</code> format
   */
  @Override
  public String toString () {
    Timestamp t = new Timestamp(70, 0, 1,
                                this.getHours(), this.getMinutes(), this.getSeconds(),
                                this.nanos);
    String s = t.toString();

    String[] split = s.split(" ");
    return split[1];
  }
}
