package org.apereo.cas.util.gen;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The default numeric generator for generating long values. Implementation
 * allows for wrapping (to restart count) if the maximum is reached.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultLongNumericGenerator implements LongNumericGenerator {

    /** The maximum length the string can be. */
    private static final int MAX_STRING_LENGTH = Long.toString(Long.MAX_VALUE)
        .length();

    /** The minimum length the String can be. */
    private static final int MIN_STRING_LENGTH = 1;

    private final AtomicLong count;

    /**
     * Instantiates a new default long numeric generator.
     */
    public DefaultLongNumericGenerator() {
        this(0);
        // nothing to do
    }

    /**
     * Instantiates a new default long numeric generator.
     *
     * @param initialValue the initial value
     */
    public DefaultLongNumericGenerator(final long initialValue) {
        this.count = new AtomicLong(initialValue);
    }

    @Override
    public long getNextLong() {
        return this.getNextValue();
    }

    @Override
    public String getNextNumberAsString() {
        return Long.toString(this.getNextValue());
    }

    @Override
    public int maxLength() {
        return DefaultLongNumericGenerator.MAX_STRING_LENGTH;
    }
    
    @Override
    public int minLength() {
        return DefaultLongNumericGenerator.MIN_STRING_LENGTH;
    }

    
    /**
     * Gets the next value.
     *
     * @return the next value. If the count has reached {@link Long#MAX_VALUE}, 
     * then {@link Long#MAX_VALUE} is returned. Otherwise, the next increment.
     */
    protected long getNextValue() {
        if (this.count.compareAndSet(Long.MAX_VALUE, 0)) {
            return Long.MAX_VALUE;
        }
        return this.count.getAndIncrement();
    }
}
