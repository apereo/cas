/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;



/**
 * Default implementation of {@link UniqueTokenIdGenerator}
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultUniqueTokenIdGenerator implements UniqueTokenIdGenerator {

    final private RandomStringGenerator idGenerator;

    public DefaultUniqueTokenIdGenerator() {
        idGenerator = new DefaultRandomStringGenerator();
    }

    /**
     * @see org.jasig.cas.util.UniqueTokenIdGenerator#getNewTokenId()
     */
    public String getNewTokenId() {
        return idGenerator.getNewString();
    }
}