package org.jasig.cas.util;

/**
 * Interface to return a random String.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface RandomStringGenerator {

    /**
     * @return the minimum length as an int guaranteed by this generator.
     */
    int getMinLength();

    /**
     * @return the maximum length as an int guaranteed by this generator.
     */
    int getMaxLength();

    /**
     * @return the new random string
     */
    String getNewString();

    /**
     * Gets the new string as bytes.
     *
     * @return the new random string as bytes
     */
    byte[] getNewStringAsBytes();
}
