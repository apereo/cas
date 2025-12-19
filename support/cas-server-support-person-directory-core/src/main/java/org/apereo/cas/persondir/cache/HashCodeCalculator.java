package org.apereo.cas.persondir.cache;

import module java.base;
import lombok.Getter;

/**
 * Builds the checksum and hash code needed to create a
 * {@link HashCodeCacheKey}.
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 * @since 7.1.0
 */
@Getter
class HashCodeCalculator {

    private static final int INITIAL_HASH = 17;

    private static final int MULTIPLIER = 37;

    private static final int SHIFT = 16;

    private long checkSum;

    /**
     * Counts the number of times {@link #append(int)} is executed.
     */
    private int count;

    /**
     * Hash code to build.
     */
    private int hashCode = INITIAL_HASH;

    /**
     * Recalculates {@link #checkSum} and
     * {@link #hashCode} using the specified value.
     *
     * @param value the specified value.
     */
    public void append(final int value) {
        count++;
        var valueToAppend = count * value;
        hashCode = MULTIPLIER * hashCode + (valueToAppend ^ (valueToAppend >>> SHIFT));
        checkSum += valueToAppend;
    }
}
