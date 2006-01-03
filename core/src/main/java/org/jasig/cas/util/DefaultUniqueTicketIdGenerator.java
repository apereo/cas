/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}. Implementation
 * utilizes a DefaultLongNumericGeneraor and a DefaultRandomStringGenerator to
 * construct the ticket id.
 * <p>
 * Tickets are of the forma [PREFIX]-[SEQUENCE NUMBER]-[RANDOM STRING]
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultUniqueTicketIdGenerator implements
    UniqueTicketIdGenerator {

    /** The numeric generator to generate the static part of the id. */
    private final NumericGenerator numericGenerator;

    /** The RandomStringGenerator to generate the secure random part of the id. */
    private final RandomStringGenerator randomStringGenerator;

    /**
     * Optional suffix to ensure uniqueness across JVMs by specifying unique
     * values.
     */
    private String suffix;

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with default values
     * including a {@link DefaultLongNumericGenerator} with a starting value of
     * 1.
     */
    public DefaultUniqueTicketIdGenerator() {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator();
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     * 
     * @param maxLength the maximum length of the random string used to generate
     * the id.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength) {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator(maxLength);
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with default values
     * including a {@link DefaultLongNumericGenerator} with a starting value of
     * 1.
     * 
     * @param suffix the value to append at the end of the unique id to ensure
     * uniqueness across JVMs.
     */
    public DefaultUniqueTicketIdGenerator(final String suffix) {
        this();
        this.suffix = suffix;
    }

    /**
     * Creates an instance of DefaultUniqueTicketIdGenerator with a specified
     * maximum length for the random portion.
     * 
     * @param maxLength the maximum length of the random string used to generate
     * the id.
     * @param suffix the value to append at the end of the unique id to ensure
     * uniqueness across JVMs.
     */
    public DefaultUniqueTicketIdGenerator(final int maxLength,
        final String suffix) {
        this(maxLength);
        this.suffix = suffix;
    }

    public String getNewTicketId(final String prefix) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);
        buffer.append("-");
        buffer.append(this.numericGenerator.getNextNumberAsString());
        buffer.append("-");
        buffer.append(this.randomStringGenerator.getNewString());

        if (this.suffix != null) {
            buffer.append("-");
            buffer.append(this.suffix);
        }

        return buffer.toString();
    }
}
