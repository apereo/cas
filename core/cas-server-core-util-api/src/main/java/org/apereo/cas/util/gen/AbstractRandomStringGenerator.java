package org.apereo.cas.util.gen;

import org.apereo.cas.util.RandomUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * This is {@link AbstractRandomStringGenerator}.
 * <p>
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Timur Duehr
 * @since 5.2.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractRandomStringGenerator implements RandomStringGenerator {
    /**
     * An instance of secure random to ensure randomness is secure.
     */
    protected final SecureRandom randomizer = RandomUtils.getNativeInstance();

    /**
     * Default string length before encoding.
     */
    protected final long defaultLength;

    /**
     * Instantiates a new default random string generator
     * with length set to {@link RandomStringGenerator#DEFAULT_LENGTH}.
     */
    protected AbstractRandomStringGenerator() {
        this(DEFAULT_LENGTH);
    }

    @Override
    public String getAlgorithm() {
        return randomizer.getAlgorithm();
    }

    @Override
    public String getNewString(final int size) {
        val random = getNewStringAsBytes(size);
        return convertBytesToString(random);
    }

    @Override
    public String getNewString() {
        return getNewString(Long.valueOf(getDefaultLength()).intValue());
    }

    @Override
    public byte[] getNewStringAsBytes(final int size) {
        val random = new byte[size];
        this.randomizer.nextBytes(random);
        return random;
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
}
