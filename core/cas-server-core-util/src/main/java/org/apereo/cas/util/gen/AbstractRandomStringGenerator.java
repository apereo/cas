package org.apereo.cas.util.gen;

import org.apereo.cas.util.RandomUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * This is {@link AbstractRandomStringGenerator}.
 *
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Timur Duehr

 * @since 5.2.0
 */
public abstract class AbstractRandomStringGenerator implements RandomStringGenerator{
    /** An instance of secure random to ensure randomness is secure. */
    protected final SecureRandom randomizer = RandomUtils.getInstanceNative();

    /** Default string length before encoding. */
    protected final int defaultLength;

    /**
     * Instantiates a new default random string generator
     * with length set to {@link RandomStringGenerator#DEFAULT_LENGTH}.
     */
    public AbstractRandomStringGenerator() {
        this.defaultLength = DEFAULT_LENGTH;
    }

    /**
     * Instantiates a new default random string generator.
     *
     * @param defaultLength the max random length
     */
    public AbstractRandomStringGenerator(final int defaultLength) {
        this.defaultLength = defaultLength;
    }

    @Override
    public int getDefaultLength() {
        return defaultLength;
    }

    @Override
    public String getAlgorithm() {
        return randomizer.getAlgorithm();
    }

    /**
     * Converts byte[] to String by simple cast. Subclasses should override.
     *
     * @param random raw bytes
     * @return a converted String
     */
    protected String convertBytesToString(final byte[] random) {
        return new String(random, StandardCharsets.UTF_8);
    }

    @Override
    public String getNewString(final int size) {
        final byte[] random = getNewStringAsBytes(size);
        return this.convertBytesToString(random);
    }

    @Override
    public String getNewString() {
        return getNewString(this.getDefaultLength());
    }

    @Override
    public byte[] getNewStringAsBytes(final int size) {
        final byte[] random = new byte[size];
        this.randomizer.nextBytes(random);
        return random;
    }

    @Override
    public byte[] getNewStringAsBytes() {
        return this.getNewStringAsBytes(this.getDefaultLength());
    }
}
