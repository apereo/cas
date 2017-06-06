package org.apereo.cas.util.spring;

import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;

/**
 * This is {@link Converters}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Converters {

    protected Converters() {}

    /**
     * The Zoned date time to string converter
     * turns a {@link ZonedDateTime} into a formatted string.
     */
    public static class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {
        @Override
        public String convert(final ZonedDateTime zonedDateTime) {
            return zonedDateTime.toString();
        }
    }
}
