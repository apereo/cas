package org.apereo.cas.util.jpa;

import lombok.val;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * This is {@link SkippingNanoSecondsLocalDateTimeConverter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Converter
public class SkippingNanoSecondsLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(final LocalDateTime dt) {
        val result = LocalDateTime.of(dt.toLocalDate(), LocalTime.of(dt.getHour(), dt.getMinute(), dt.getSecond()));
        return Timestamp.valueOf(result);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(final Timestamp timestamp) {
        val dt = timestamp.toLocalDateTime();
        return LocalDateTime.of(dt.toLocalDate(), LocalTime.of(dt.getHour(), dt.getMinute(), dt.getSecond()));
    }
}
