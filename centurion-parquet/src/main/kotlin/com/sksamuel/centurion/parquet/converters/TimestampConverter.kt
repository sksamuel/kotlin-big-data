package com.sksamuel.centurion.parquet.converters

import com.sksamuel.centurion.Schema
import jodd.time.JulianDate
import org.apache.parquet.example.data.simple.NanoTime
import org.apache.parquet.io.api.Binary
import org.apache.parquet.io.api.PrimitiveConverter
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneOffset

class TimestampConverter(
   schema: Schema,
   private val index: Int,
   private val collector: ValuesCollector,
) : PrimitiveConverter() {

  // The Julian Date (JD) is the number of days (with decimal fraction of the day) that
  // have elapsed since 12 noon UTC on the Julian epoch
  private val nanosInDay = 24L * 60L * 60 * 1000 * 1000 * 1000

   // In data annotated with the TIMESTAMP logical type, each value is a single int64 number
   // that can be decoded into year, month, day, hour, minute, second and subsecond fields
   // using calculations detailed below.
   //
   // Please note that a value defined this way does not necessarily correspond to a single
   // instant on the time-line and such interpertations are allowed on purpose.
   //
   //The TIMESTAMP type has two type parameters:
   //
   // isAdjustedToUTC must be either true or false.
   // unit must be one of MILLIS, MICROS or NANOS. This list is subject to potential expansion in the future.
   // Upon reading, unknown unit-s must be handled as unsupported features (rather than as errors in the data files).
   //

   override fun addLong(value: Long) {
      collector[index] = Instant.ofEpochMilli(value)
   }

  /**
   * Since INT96 is sometimes used as timestamps too
   * See https://issues.apache.org/jira/browse/PARQUET-323
   * https://issues.apache.org/jira/browse/PARQUET-861
   * https://stackoverflow.com/questions/42628287/sparks-int96-time-type
   *
   *  Timestamps saved as an `int96` are made up of the nanoseconds in the day
   *  (first 8 byte) and the Julian day (last 4 bytes). No timezone is attached to this value.
   *  To convert the timestamp into nanoseconds since the Unix epoch, 00:00:00.000000
   *  on 1 January 1970, the following formula can be used:
   *  `(julian_day - 2440588) * (86400 * 1000 * 1000 * 1000) + nanoseconds`.
   *  The magic number `2440588` is the julian day for 1 January 1970.
   *
   * Note that these timestamps are the common usage of the `int96` physical type and are not
   * marked with a special logical type annotation.
   */
  override fun addBinary(value: Binary) {
    // the first arg is the number of days since Monday, January 1, 4713 BC
    // the second arg is the decimal fraction between 0 and 1 of the number of elapsed nanos, with
    // 0.0 being midnight and 1.0 being 1 nanosecond before the next day
    val nano = NanoTime.fromBinary(value)
    val julianDate = JulianDate(nano.julianDay, nano.timeOfDayNanos.toDouble() / nanosInDay)
    val ts = Timestamp.from(julianDate.toLocalDateTime().toInstant(ZoneOffset.UTC))
    //   val nanos = (nano.julianDay.toLong() - 2440588L) * (86400L * 1000 * 1000 * 1000) + nano.timeOfDayNanos
//    val millis = nanos / 1000 / 1000
//    val ts = Timestamp.from(Instant.ofEpochMilli(millis))
    collector[index] = ts
  }
}
