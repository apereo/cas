/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * The default numeric generator for generating long values. Implementation
 * allows for wrapping (to restart count) if the maximum is reached.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultLongNumericGenerator implements LongNumericGenerator {

    /** The maximum length the string can be. */
    private static final int MAX_STRING_LENGTH = Long.toString(Long.MAX_VALUE)
        .length();

    /** The minimum length the String can be. */
    private static final int MIN_STRING_LENGTH = 1;

    /** The default wrap value of true. */
    private static final boolean DEFAULT_WRAP = true;

    /** The default initial value of 0. */
    private static final long DEFAULT_INTITIAL_VALUE = 0;

    /** Whether to wrap or not when we reach the maximum value. */
    private boolean wrap;

    /** The current number we are at. */
    private long count = 0;

    public DefaultLongNumericGenerator() {
        this.wrap = DEFAULT_WRAP;
    }

    public DefaultLongNumericGenerator(final boolean wrap) {
        this(DEFAULT_INTITIAL_VALUE, wrap);
    }

    public DefaultLongNumericGenerator(final long initialValue,
        final boolean wrap) {
        this.wrap = wrap;
        this.count = initialValue;
    }

    public DefaultLongNumericGenerator(final long initialValue) {
        this(initialValue, DEFAULT_WRAP);
    }

    public long getNextLong() {
        return this.getNextValue();
    }

    public String getNextNumberAsString() {
        return Long.toString(this.getNextValue());
    }

    public int maxLength() {
        return DefaultLongNumericGenerator.MAX_STRING_LENGTH;
    }

    public int minLength() {
        return DefaultLongNumericGenerator.MIN_STRING_LENGTH;
    }

    /**
     * @throws IllegalStateException if the maximum value is reached and
     * wrapping is not allowed.
     */
    protected synchronized long getNextValue() {
        if (!this.wrap && this.count == Long.MAX_VALUE) {
            throw new IllegalStateException(
                "Maximum value reached for this number generator.");
        }

        if (this.count == Long.MAX_VALUE) {
            this.count = 0;
        }

        return ++this.count;
    }
}
