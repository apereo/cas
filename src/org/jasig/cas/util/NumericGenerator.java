package org.jasig.cas.util;

/**
 * Interface to return a new sequential number for each call.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface NumericGenerator {

    /**
     * Method to retrieve the next number as a String.
     * 
     * @return
     */
    String getNextNumberAsString();

    /**
     * The guaranteed maximum length of a String returned by this generator
     * 
     * @return the maximum length
     */
    int maxLength();

    /**
     * The guaranteed minimum length of a String returned by this generator.
     * 
     * @return the minimum length.
     */
    int minLength();
}