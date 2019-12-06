package org.apereo.cas.util.model;

import lombok.Builder;
import lombok.Getter;
import lombok.val;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * This is {@link Capacity}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Builder
public class Capacity implements Serializable {
    private static final long serialVersionUID = -331719796564884951L;

    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+(\\.\\d+)*)\\s*(\\S+)");

    private final UnitOfMeasure unitOfMeasure;

    private final Double size;

    public static Capacity parse(final String capacity) {
        val matcher = SIZE_PATTERN.matcher(capacity);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid capacity definition: " + capacity);
        }
        val count = Double.parseDouble(matcher.group(1));
        val unit = UnitOfMeasure.valueOf(matcher.group(3).toUpperCase());
        return Capacity.builder().unitOfMeasure(unit).size(count).build();
    }

    /**
     * Capacity units of measure.
     */
    public enum UnitOfMeasure {
        /**
         * Bytes.
         */
        B,
        /**
         * KiloBytes.
         */
        KB,
        /**
         * MegaBytes.
         */
        MB,
        /**
         * GigaBytes.
         */
        GB,
        /**
         * TeraBytes.
         */
        TB
    }
}
