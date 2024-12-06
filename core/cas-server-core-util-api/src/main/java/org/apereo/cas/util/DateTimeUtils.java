package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Date/Time utility methods.
 *
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
                result = LocalDateTime.parse(value.toUpperCase(Locale.ENGLISH), DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
            } catch (final Exception e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = LocalDateTime.parse(value.toUpperCase(Locale.ENGLISH), DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a"));
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
     * Local date time local date.
     *
     * @param time the time
     * @return the local date
     */
    public static LocalDate localDateOf(final long time) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }

    /**
     * Parse the given value as a zoned datetime.
     *
     * @param value the value
     * @return the date/time instance
     */
    public static ZonedDateTime zonedDateTimeOf(final String value) {
        val parsers = List.of(DateTimeFormatter.ISO_ZONED_DATE_TIME, DateTimeFormatter.RFC_1123_DATE_TIME);
        return parsers
            .stream()
            .map(parser -> FunctionUtils.doAndHandle(() -> ZonedDateTime.parse(value, parser), throwable -> null).get())
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
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
        return Optional.ofNullable(time).map(instant -> instant.atZone(ZoneOffset.UTC)).orElse(null);
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
        return Optional.ofNullable(time).map(date -> zonedDateTimeOf(Instant.ofEpochMilli(date.getTime()))).orElse(null);
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
     * Convert to zoned date time.
     *
     * @param value the value
     * @return the zoned date time
     */
    public static ZonedDateTime convertToZonedDateTime(final String value) {
        val dt = zonedDateTimeOf(value);
        if (dt != null) {
            return dt;
        }
        val lt = localDateTimeOf(value);
        return zonedDateTimeOf(lt.atZone(ZoneOffset.UTC));
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
        return switch (tu) {
            case DAYS -> TimeUnit.DAYS;
            case HOURS -> TimeUnit.HOURS;
            case MINUTES -> TimeUnit.MINUTES;
            case SECONDS -> TimeUnit.SECONDS;
            case MICROS -> TimeUnit.MICROSECONDS;
            case MILLIS -> TimeUnit.MILLISECONDS;
            case NANOS -> TimeUnit.NANOSECONDS;
            default -> throw new UnsupportedOperationException("Temporal unit is not supported");
        };
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
        return switch (tu) {
            case DAYS -> ChronoUnit.DAYS;
            case HOURS -> ChronoUnit.HOURS;
            case MINUTES -> ChronoUnit.MINUTES;
            case MICROSECONDS -> ChronoUnit.MICROS;
            case MILLISECONDS -> ChronoUnit.MILLIS;
            case NANOSECONDS -> ChronoUnit.NANOS;
            case SECONDS -> ChronoUnit.SECONDS;
        };
    }
}
