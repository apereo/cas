/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Strategy for generating unique tokens
 * 
 * @author Scott Battaglia
 * @version $Id: UniqueTokenIdGenerator.java,v 1.1 2005/02/15 05:06:38
 * sbattaglia Exp $
 */
public interface UniqueTokenIdGenerator {

    /**
     * Generate unique token id.
     * 
     * @return unique token id.
     */
    String getNewTokenId();
}