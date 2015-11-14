package org.jasig.cas.util;

import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A fast date format based on the ISO-8601 standard.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class ISOStandardDateFormat extends FastDateFormat {

    private static final long serialVersionUID = 9196017562782775535L;

    /** The ISO date format used by this formatter. */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Instantiates a new ISO standard date format
     * based on the format {@link #DATE_FORMAT}.
     */
    public ISOStandardDateFormat() {
        super(DATE_FORMAT, TimeZone.getDefault(), Locale.getDefault());
    }

    
    /**
     * Gets the current date and time
     * formatted by the pattern specified.
     *
     * @return the current date and time
     */
    public String getCurrentDateAndTime() {
        return format(new Date());
    }

    /**
     * Format the datetime given.
     *
     * @param dt the datetime
     * @return the date and time
     */
    public String format(final DateTime dt) {
        return format(dt.toDate());
    }
}
