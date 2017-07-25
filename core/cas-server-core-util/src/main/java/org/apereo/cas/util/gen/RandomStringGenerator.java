package org.apereo.cas.util.gen;

/**
 * Interface to return a random String.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface RandomStringGenerator {

    /** The default length. */
    int DEFAULT_LENGTH = 36;

    /**
     * @return the default length as an int.
     */
    int getDefaultLength();

    /**
     * @return a new random string of specified initial size
     */
    String getNewString(int size);

    /**
     * @return a new random string of default initial size
     */
    String getNewString();

    /**
     * Gets the new string as bytes.
     *
     * @return the new random string as bytes
     */
    byte[] getNewStringAsBytes();

    /**
     * Gets the new string as bytes.
     *
     * @param size the size of return
     * @return the new random string as bytes
     */
    byte[] getNewStringAsBytes(final int size);
}
