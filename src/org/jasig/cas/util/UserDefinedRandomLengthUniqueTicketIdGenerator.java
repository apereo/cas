/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Default implementation of {@link UniqueTicketIdGenerator}
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class UserDefinedRandomLengthUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    final private NumericGenerator numericGenerator;

    final private RandomStringGenerator randomStringGenerator;

    public UserDefinedRandomLengthUniqueTicketIdGenerator(int randomLength) {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new UserDefinedLengthRandomStringGenerator(randomLength);
    }

    /**
     * @see org.jasig.cas.util.UniqueTicketIdGenerator#getNewTicketId(java.lang.String)
     */
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