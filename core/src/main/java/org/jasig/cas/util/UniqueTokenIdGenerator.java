/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Strategy for generating unique tokens.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface UniqueTokenIdGenerator {

    /**
     * Generate unique token id.
     * 
     * @return unique token id.
     */
    String getNewTokenId();
}
