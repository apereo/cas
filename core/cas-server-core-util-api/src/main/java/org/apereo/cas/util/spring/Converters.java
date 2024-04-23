package org.apereo.cas.util.spring;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.NoArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;

import java.time.ZonedDateTime;

/**
 * This is {@link Converters}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
public class Converters {

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

    /**
     * Converts a String to a resource instance.
     */
    public static class StringToResourceConverter implements Converter<String, Resource> {

        @Override
        public Resource convert(final String resource) {
            return FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(resource));
        }
    }
}
