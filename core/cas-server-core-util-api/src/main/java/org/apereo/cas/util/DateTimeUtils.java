package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@UtilityClass
public class DateTimeUtils {


    /**
     * Parse the given value as a local datetime.
     *
     * @param value the value
     * @return the date/time instance
     */
    public static LocalDateTime localDateTimeOf(final String value) {
        var result = (LocalDateTime) null;

        try {
            result = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (final Exception e) {
            result = null;
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value);
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value.toUpperCase(), DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value.toUpperCase(), DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a"));
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                val ld = LocalDate.parse(value, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                result = LocalDateTime.of(ld, LocalTime.now(ZoneId.systemDefault()));
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                val ld = LocalDate.parse(value);
                result = LocalDateTime.of(ld, LocalTime.now(ZoneId.systemDefault()));
            } catch (final Exception e) {
                result = null;
            }
        }
        return result;
    }

    /**
     * Local date time of local date time.
     *
     * @param time the time
     * @return the local date time
     */
    public static LocalDateTime localDateTimeOf(final long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }

    /**
     * Local date time of local date time.
     *
     * @param time the time
     * @return the local date time
     */
    public static LocalDateTime localDateTimeOf(final Date time) {
        return localDateTimeOf(time.getTime());
    }

    /**
     * Parse the given value as a zoned datetime.
     *
     * @param value the value
     * @return the date/time instance
     */
    public static ZonedDateTime zonedDateTimeOf(final String value) {
        try {
            return ZonedDateTime.parse(value);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Utility for creating a ZonedDateTime object from a ZonedDateTime.
     *
     * @param time ZonedDateTime to be copied
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final TemporalAccessor time) {
        return ZonedDateTime.from(time);
    }

    /**
     * Zoned date time.
     *
     * @param time the time
     * @return the zoned date time
     */
    public static ZonedDateTime zonedDateTimeOf(final Instant time) {
        return time != null ? ZonedDateTime.from(time.atZone(ZoneOffset.UTC)) : null;
    }

    /**
     * Gets ZonedDateTime for ReadableInstant.
     *
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final ReadableInstant time) {
        return zonedDateTimeOf(Instant.ofEpochMilli(time.getMillis()));
    }

    /**
     * Utility for creating a ZonedDateTime object from a millisecond timestamp.
     *
     * @param time Milliseconds since Epoch UTC
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final long time) {
        return zonedDateTimeOf(time, ZoneOffset.UTC);
    }

    /**
     * Utility for creating a ZonedDateTime object from a millisecond timestamp.
     *
     * @param time   Milliseconds since Epoch
     * @param zoneId Time zone
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final long time, final ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), zoneId);
    }

    /**
     * Gets ZonedDateTime for Date.
     *
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final Date time) {
        return zonedDateTimeOf(Instant.ofEpochMilli(time.getTime()));
    }

    /**
     * Gets ZonedDateTime for Calendar.
     *
     * @param time Time object to be converted.
     * @return ZonedDateTime representing time
     */
    public static ZonedDateTime zonedDateTimeOf(final Calendar time) {
        return ZonedDateTime.ofInstant(time.toInstant(), time.getTimeZone().toZoneId());
    }

    /**
     * Gets DateTime for Instant.
     *
     * @param time Time object to be converted.
     * @return DateTime representing time
     */
    public static DateTime dateTimeOf(final Instant time) {
        return new DateTime(time.toEpochMilli());
    }

    /**
     * Gets DateTime for ZonedDateTime.
     *
     * @param time Time object to be converted.
     * @return DateTime representing time
     */
    public static DateTime dateTimeOf(final ChronoZonedDateTime time) {
        return dateTimeOf(time.toInstant());
    }

    /**
     * Gets Date for ZonedDateTime.
     *
     * @param time Time object to be converted.
     * @return Date representing time
     */
    public static Date dateOf(final ChronoZonedDateTime time) {
        return dateOf(time.toInstant());
    }

    /**
     * Date of local date.
     *
     * @param time the time
     * @return the date
     */
    public static Date dateOf(final LocalDate time) {
        return Date.from(time.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    /**
     * Date of local date time.
     *
     * @param time the time
     * @return the date
     */
    public static Date dateOf(final LocalDateTime time) {
        return dateOf(time.toInstant(ZoneOffset.UTC));
    }

    /**
     * Gets Date for Instant.
     *
     * @param time Time object to be converted.
     * @return Date representing time
     */
    public static Date dateOf(final Instant time) {
        return Date.from(time);
    }

    /**
     * Gets Timestamp for ZonedDateTime.
     *
     * @param time Time object to be converted.
     * @return Timestamp representing time
     */
    public static Timestamp timestampOf(final ChronoZonedDateTime time) {
        return timestampOf(time.toInstant());
    }

    /**
     * Gets Timestamp for Instant.
     *
     * @param time Time object to be converted.
     * @return Timestamp representing time
     */
    private static Timestamp timestampOf(final Instant time) {
        return Timestamp.from(time);
    }

    /**
     * Convert to zoned date time.
     *
     * @param value the value
     * @return the zoned date time
     */
    public static ZonedDateTime convertToZonedDateTime(final String value) {
        val dt = DateTimeUtils.zonedDateTimeOf(value);
        if (dt != null) {
            return dt;
        }
        val lt = DateTimeUtils.localDateTimeOf(value);
        return DateTimeUtils.zonedDateTimeOf(lt.atZone(ZoneOffset.UTC));
    }

    /**
     * To time unit time unit.
     *
     * @param tu the tu
     * @return the time unit
     */
    public static TimeUnit toTimeUnit(final ChronoUnit tu) {
        if (tu == null) {
            return null;
        }
        switch (tu) {
            case DAYS:
                return TimeUnit.DAYS;
            case HOURS:
                return TimeUnit.HOURS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case NANOS:
                return TimeUnit.NANOSECONDS;
            default:
                throw new UnsupportedOperationException("Temporal unit is not supported");
        }
    }

    /**
     * To chrono unit.
     *
     * @param tu the tu
     * @return the chrono unit
     */
    public static ChronoUnit toChronoUnit(final TimeUnit tu) {
        if (tu == null) {
            return null;
        }
        switch (tu) {
            case DAYS:
                return ChronoUnit.DAYS;
            case HOURS:
                return ChronoUnit.HOURS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            default:
                return null;
        }
    }
}
