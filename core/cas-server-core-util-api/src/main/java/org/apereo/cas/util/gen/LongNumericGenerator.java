package org.apereo.cas.util.gen;

/**
 * Interface to guaranteed to return a long.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface LongNumericGenerator extends NumericGenerator {

    /**
     * Get the next long in the sequence.
     *
     * @return the next long in the sequence.
     */
    long getNextLong();
}
