package org.jasig.cas.util;

/**
 * Interface to guarantee to return a long.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface LongNumericGenerator extends NumericGenerator {

    long getNextLong();
}
