/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    final private NumericGenerator numericGenerator;

    final private RandomStringGenerator randomStringGenerator;

    public DefaultUniqueTicketIdGenerator() {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator();
    }

    public DefaultUniqueTicketIdGenerator(int maxLength) {
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