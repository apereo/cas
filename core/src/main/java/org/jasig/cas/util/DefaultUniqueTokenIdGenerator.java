/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Default implementation of {@link UniqueTokenIdGenerator}.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultUniqueTokenIdGenerator implements UniqueTokenIdGenerator {

    /** The RandomStringGenerator to be used to generate tokens. */
    private final RandomStringGenerator idGenerator;

    public DefaultUniqueTokenIdGenerator() {
        this.idGenerator = new DefaultRandomStringGenerator();
    }

    public String getNewTokenId() {
        return this.idGenerator.getNewString();
    }
}
