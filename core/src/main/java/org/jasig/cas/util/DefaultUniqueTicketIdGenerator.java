/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}.
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

    public DefaultUniqueTicketIdGenerator() {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator();
    }

    public DefaultUniqueTicketIdGenerator(final int maxLength) {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator(maxLength);
    }

    public String getNewTicketId(final String prefix) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);
        buffer.append("-");
        buffer.append(this.numericGenerator.getNextNumberAsString());
        buffer.append("-");
        buffer.append(this.randomStringGenerator.getNewString());
        return buffer.toString();
    }

}
