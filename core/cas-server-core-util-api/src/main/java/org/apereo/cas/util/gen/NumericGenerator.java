package org.apereo.cas.util.gen;

/**
 * Interface to return a new sequential number for each call.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface NumericGenerator {

    /**
     * Method to retrieve the next number as a String.
     *
     * @return the String representation of the next number in the sequence
     */
    String getNextNumberAsString();

    /**
     * The guaranteed maximum length of a String returned by this generator.
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
