package org.jasig.cas.util;

/**
 * Interface to guarantee to return a long.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface LongNumericGenerator extends NumericGenerator {

    /**
     * Get the next long in the sequence.
     * 
     * @return the next long in the sequence.
     */
    long getNextLong();
}