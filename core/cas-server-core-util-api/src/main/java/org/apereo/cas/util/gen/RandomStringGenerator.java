package org.apereo.cas.util.gen;

/**
 * Interface to return a random String.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public interface RandomStringGenerator {

    /**
     * The default length.
     */
    int DEFAULT_LENGTH = 36;

    /**
     * Default length to use.
     * @return the default length as an int.
     */
    long getDefaultLength();

    /**
     * The algorithm used by the generator's SecureRandom instance.
     * @return the algorithm used by the generator's SecureRandom instance.
     */
    String getAlgorithm();

    /**
     * A new random string of specified initial size.
     * @param size length of random string before encoding
     * @return a new random string of specified initial size
     */
    String getNewString(int size);

    /**
     * A new random string of specified default size.
     * @return a new random string of default initial size
     */
    String getNewString();

    /**
     * Gets the new string as bytes.
     *
     * @param size the size of return
     * @return the new random string as bytes
     */
    byte[] getNewStringAsBytes(int size);
}
