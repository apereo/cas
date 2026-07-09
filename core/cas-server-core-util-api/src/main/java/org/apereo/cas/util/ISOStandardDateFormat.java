package org.apereo.cas.util;

import module java.base;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * A fast date format based on the ISO-8601 standard.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ISOStandardDateFormat extends FastDateFormat {

    @Serial
    private static final long serialVersionUID = 9196017562782775535L;

    /**
     * The ISO date format used by this formatter.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Instantiates a new ISO standard date format
     * based on the format {@link #DATE_FORMAT}.
     *
     * @deprecated Use {@link #ISOStandardDateFormat(ZoneId, Locale)} or {@link #ISOStandardDateFormat(ZoneId)}.
     */
    @Deprecated(since = "8.1.0")
    public ISOStandardDateFormat() {
        this(ZoneId.systemDefault(), Locale.getDefault());
    }

    public ISOStandardDateFormat(final ZoneId timezone, final Locale locale) {
        super(DATE_FORMAT, TimeZone.getTimeZone(timezone), locale);
    }

    public ISOStandardDateFormat(final ZoneId timezone) {
        this(timezone, Locale.getDefault());
    }

    /**
     * Gets the current date and time
     * formatted by the pattern specified.
     *
     * @return the current date and time
     */
    public String getCurrentDateAndTime() {
        return format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Format the datetime given.
     *
     * @param dt the datetime
     * @return the date and time
     */
    public String format(final ZonedDateTime dt) {
        return format(DateTimeUtils.dateOf(dt));
    }

    /**
     * Format the datetime given.
     *
     * @param dt the datetime
     * @return the date and time
     */
    public String format(final Instant dt) {
        return format(DateTimeUtils.dateOf(dt));
    }
}
