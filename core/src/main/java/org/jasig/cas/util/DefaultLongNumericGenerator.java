/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultLongNumericGenerator implements LongNumericGenerator {

    private static final int MAX_STRING_LENGTH = Long.toString(Long.MAX_VALUE)
        .length();

    private static final int MIN_STRING_LENGTH = 1;

    private static final boolean DEFAULT_WRAP = true;

    private boolean wrap;

    private long count = 0;

    public DefaultLongNumericGenerator() {
        this.wrap = DEFAULT_WRAP;
    }

    public DefaultLongNumericGenerator(boolean wrap) {
        this.wrap = wrap;
    }

    public DefaultLongNumericGenerator(long initialValue, boolean wrap) {
        this.count = initialValue;
        this.wrap = wrap;
    }

    public DefaultLongNumericGenerator(long initialValue) {
        this.count = initialValue;
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

    protected synchronized long getNextValue() throws IllegalStateException {
        if (!this.wrap && this.count == Long.MAX_VALUE)
            throw new IllegalStateException(
                "Maximum value reached for this number generator.");

        if (this.count == Long.MAX_VALUE) {
            this.count = 0;
        }

        return ++this.count;
    }
}