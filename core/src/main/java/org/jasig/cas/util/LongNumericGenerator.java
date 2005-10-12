/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Interface to guaranteed to return a long.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface LongNumericGenerator extends NumericGenerator {

    /**
     * Get the next long in the sequence.
     * 
     * @return the next long in the sequence.
     */
    long getNextLong();
}
