package org.jasig.cas.util;

import java.security.SecureRandom;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public final class DefaultRandomStringGenerator implements RandomStringGenerator {

    /** The default maximum length. */
    protected static final int DEFAULT_MAX_RANDOM_LENGTH = 35;

    /** The array of printable characters to be used in our random string. */
    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679"
        .toCharArray();

    /** An instance of secure random to ensure randomness is secure. */
    private final SecureRandom randomizer = new SecureRandom();

    /** The maximum length the random string can be. */
    private final int maximumRandomLength;

    /**
     * Instantiates a new default random string generator
     * with length set to {@link #DEFAULT_MAX_RANDOM_LENGTH}.
     */
    public DefaultRandomStringGenerator() {
        this.maximumRandomLength = DEFAULT_MAX_RANDOM_LENGTH;
    }

    /**
     * Instantiates a new default random string generator.
     *
     * @param maxRandomLength the max random length
     */
    public DefaultRandomStringGenerator(final int maxRandomLength) {
        this.maximumRandomLength = maxRandomLength;
    }

    @Override
    public int getMinLength() {
        return this.maximumRandomLength;
    }

    @Override
    public int getMaxLength() {
        return this.maximumRandomLength;
    }

    @Override
    public String getNewString() {
        final byte[] random = getNewStringAsBytes();

        return convertBytesToString(random);
    }

    @Override
    public byte[] getNewStringAsBytes() {
        final byte[] random = new byte[this.maximumRandomLength];

        this.randomizer.nextBytes(random);

        return random;
    }

    /**
     * Convert bytes to string, taking into account {@link #PRINTABLE_CHARACTERS}.
     *
     * @param random the random
     * @return the string
     */
    private static String convertBytesToString(final byte[] random) {
        final char[] output = new char[random.length];
        for (int i = 0; i < random.length; i++) {
            final int index = Math.abs(random[i] % PRINTABLE_CHARACTERS.length);
            output[i] = PRINTABLE_CHARACTERS[index];
        }

        return new String(output);
    }
}
