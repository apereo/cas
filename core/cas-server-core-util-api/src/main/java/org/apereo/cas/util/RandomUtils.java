package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.IntStream;

/**
 * This is {@link RandomUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class RandomUtils {
    private static final int HEX_HIGH_BITS_BITWISE_FLAG = 0x0f;
    private static final int SECURE_ID_CHARS_LENGTH = 40;
    private static final int SECURE_ID_BYTES_LENGTH = 20;
    private static final int SECURE_ID_SHIFT_LENGTH = 4;
    private static final String NATIVE_NON_BLOCKING_ALGORITHM = "NativePRNGNonBlocking";

    /**
     * Get strong enough SecureRandom instance and of the checked exception.
     *
     * @return the strong instance
     */
    public static SecureRandom getNativeInstance() {
        try {
            return SecureRandom.getInstance(NATIVE_NON_BLOCKING_ALGORITHM);
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.trace(e.getMessage(), e);
            return new SecureRandom();
        }
    }

    /**
     * Next long between 0 and long's maximum value.
     *
     * @return the long
     */
    public static long nextLong() {
        return nextLong(0, Long.MAX_VALUE);
    }

    public static long nextLong(final long startInclusive, final long endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive,
            "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return (long) nextDouble(startInclusive, endExclusive);
    }

    public static double nextDouble(final double startInclusive, final double endInclusive) {
        Validate.isTrue(endInclusive >= startInclusive,
            "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endInclusive) {
            return startInclusive;
        }

        return startInclusive + (endInclusive - startInclusive) * getNativeInstance().nextDouble();
    }

    /**
     * <p> Returns a random double within 0 - Double.MAX_VALUE </p> .
     *
     * @return the random double
     * @see #nextDouble(double, double)
     * @since 3.5
     */
    public static double nextDouble() {
        return nextDouble(0, Double.MAX_VALUE);
    }


    /**
     * Generate secure random id string.
     *
     * @return the string
     */
    public static String generateSecureRandomId() {
        val generator = getNativeInstance();
        val charMappings = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p'};

        val bytes = new byte[SECURE_ID_BYTES_LENGTH];
        generator.nextBytes(bytes);

        val chars = new char[SECURE_ID_CHARS_LENGTH];
        IntStream.range(0, bytes.length).forEach(i -> {
            val left = bytes[i] >> SECURE_ID_SHIFT_LENGTH & HEX_HIGH_BITS_BITWISE_FLAG;
            val right = bytes[i] & HEX_HIGH_BITS_BITWISE_FLAG;
            chars[i * 2] = charMappings[left];
            chars[i * 2 + 1] = charMappings[right];
        });
        return String.valueOf(chars);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of Latin alphabetic
     * characters (a-z, A-Z).</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomAlphabetic(final int count) {
        return random(count, true, false);
    }

    /**
     * <p>Creates a random string whose length is between the inclusive minimum and
     * the exclusive maximum.</p>
     *
     * <p>Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z).</p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate
     * @param maxLengthExclusive the exclusive maximum length of the string to generate
     * @return the random string
     * @since 3.5
     */
    public static String randomAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomAlphabetic(nextInt(minLengthInclusive, maxLengthExclusive));
    }


    /**
     * <p>
     * Returns a random integer within the specified range.
     * </p>
     *
     * @param startInclusive the smallest value that can be returned, must be non-negative
     * @param endExclusive   the upper bound (not included)
     * @return the random integer
     * @throws IllegalArgumentException if {@code startInclusive > endExclusive} or if
     *                                  {@code startInclusive} is negative
     */
    public static int nextInt(final int startInclusive, final int endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive,
            "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return startInclusive + getNativeInstance().nextInt(endExclusive - startInclusive);
    }

    /**
     * Returns a random int within 0 - Integer.MAX_VALUE.
     *
     * @return the random integer
     * @see #nextInt(int, int)
     * @since 3.5
     */
    public static int nextInt() {
        return nextInt(0, Integer.MAX_VALUE);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param count   the length of random string to create
     * @param letters if {@code true}, generated string may include
     *                alphabetic characters
     * @param numbers if {@code true}, generated string may include
     *                numeric characters
     * @return the random string
     */
    public static String random(final int count, final boolean letters, final boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param count   the length of random string to create
     * @param start   the position in set of chars to start at
     * @param end     the position in set of chars to end before
     * @param letters if {@code true}, generated string may include
     *                alphabetic characters
     * @param numbers if {@code true}, generated string may include
     *                numeric characters
     * @return the random string
     */
    public static String random(final int count, final int start, final int end, final boolean letters, final boolean numbers) {
        return RandomStringUtils.random(count, start, end, letters, numbers, null, getNativeInstance());
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of Latin alphabetic
     * characters (a-z, A-Z) and the digits 0-9.</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomAlphanumeric(final int count) {
        return random(count, true, true);
    }

    /**
     * <p>Creates a random string whose length is between the inclusive minimum and
     * the exclusive maximum.</p>
     *
     * <p>Characters will be chosen from the set of Latin alphabetic
     * characters (a-z, A-Z) and the digits 0-9.</p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate
     * @param maxLengthExclusive the exclusive maximum length of the string to generate
     * @return the random string
     * @since 3.5
     */
    public static String randomAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomAlphanumeric(nextInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of numeric
     * characters.</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomNumeric(final int count) {
        return random(count, false, true);
    }
}
