package org.apereo.cas.util.jpa;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * This is {@link StringToNumberAttributeConverter}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Converter
public class StringToNumberAttributeConverter implements AttributeConverter<Number, String> {

    @Override
    public String convertToDatabaseColumn(final Number number) {
        return number.toString();
    }

    @Override
    public Number convertToEntityAttribute(final String value) {
        val defaultValue = System.nanoTime();
        val result = NumberUtils.toLong(value, defaultValue);
        return result == defaultValue ? new BigInteger(value) : result;
    }
}
