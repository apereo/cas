package org.apereo.cas.util.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;

/**
 * This is {@link Converters}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
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
}
