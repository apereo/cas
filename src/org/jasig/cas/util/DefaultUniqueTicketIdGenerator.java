/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;



/**
 * Default implementation of {@link UniqueTicketIdGenerator}
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultUniqueTicketIdGenerator implements UniqueTicketIdGenerator {

    final private NumericGenerator numericGenerator;
    final private RandomStringGenerator randomStringGenerator;

    public DefaultUniqueTicketIdGenerator() {
        this.numericGenerator = new DefaultLongNumericGenerator(1);
        this.randomStringGenerator = new DefaultRandomStringGenerator();
    }

    /**
     * @see org.jasig.cas.util.UniqueTicketIdGenerator#getNewTicketId(java.lang.String)
     */
    public String getNewTicketId(final String prefix) {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(prefix);
    	buffer.append("-");
    	buffer.append(numericGenerator.getNextNumberAsString());
    	buffer.append("-");
    	buffer.append(randomStringGenerator.getNewString());
    	return buffer.toString();
    }

}