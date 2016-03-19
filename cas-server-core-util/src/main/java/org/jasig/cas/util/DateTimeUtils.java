package org.jasig.cas.util;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public class DateTimeUtils {

    private DateTimeUtils() {
    }

    /**
     * Utility for creating a ZonedDateTime object from a ZonedDateTime.
     * @param time ZonedDateTime to be copied
     * @return ZonedDateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static ZonedDateTime zonedDateTimeOf(final ZonedDateTime time){
        return ZonedDateTime.from(time);
    }


    /**
     * Utility for creating a ZonedDateTime object from a millisecond timestamp.
     * @param time Miliseconds since Epoch UTC
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final long time){
        return zonedDateTimeOf(time, ZoneOffset.UTC);
    }

    /**
     * Utility for creating a ZonedDateTime object from a millisecond timestamp.
     * @param time Miliseconds since Epoch
     * @param zoneId Time zone
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final long time, final ZoneId zoneId){
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), zoneId);
    }

    /**
     * Gets ZonedDateTime for ReadableInstant.
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static ZonedDateTime zonedDateTimeOf(final ReadableInstant time){
        return zonedDateTimeOf(time.getMillis());
    }

    /**
     * Gets ZonedDateTime for Date.
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static ZonedDateTime zonedDateTimeOf(final Date time) {
        return zonedDateTimeOf(time.getTime());
    }

    /**
     * Gets ZonedDateTime for Calendar.
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static ZonedDateTime zonedDateTimeOf(final Calendar time) {
        return ZonedDateTime.ofInstant(time.toInstant(), time.getTimeZone().toZoneId());
    }

    /**
     * Gets DateTime for Instant.
     * @param time Time object to be converted.
     * @return DateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static DateTime dateTimeOf(final Instant time){
        return new DateTime(time.toEpochMilli());
    }

    /**
     * Gets DateTime for ZonedDateTime.
     * @param time Time object to be converted.
     * @return DateTime representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static DateTime dateTimeOf(final ZonedDateTime time){
        return dateTimeOf(time.toInstant());
    }

    /**
     * Gets Date for ZonedDateTime.
     * @param time Time object to be converted.
     * @return Date representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static Date dateOf(final ZonedDateTime time){
        return dateOf(time.toInstant());
    }

    /**
     * Gets Date for Instant.
     * @param time Time object to be converted.
     * @return Date representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static Date dateOf(final Instant time){
        return Date.from(time);
    }

    /**
     * Gets Timestamp for ZonedDateTime.
     * @param time Time object to be converted.
     * @return Timestamp representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    public static Timestamp timestampOf(final ZonedDateTime time) {
        return timestampOf(time.toInstant());
    }

    /**
     * Gets Timestamp for Instant.
     * @param time Time object to be converted.
     * @return Timestamp representing time
     * @deprecated Java 8 transition utility method
     */
    @Deprecated
    private static Timestamp timestampOf(final Instant time) {
        return Timestamp.from(time);
    }
}
