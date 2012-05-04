/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import java.security.SecureRandom;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultRandomStringGenerator implements
    RandomStringGenerator {

    /** The array of printable characters to be used in our random string. */
    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679"
        .toCharArray();

    /** The default maximum length. */
    private static final int DEFAULT_MAX_RANDOM_LENGTH = 35;

    /** An instance of secure random to ensure randomness is secure. */
    private SecureRandom randomizer = new SecureRandom();

    /** The maximum length the random string can be. */
    private final int maximumRandomLength;

    public DefaultRandomStringGenerator() {
        this.maximumRandomLength = DEFAULT_MAX_RANDOM_LENGTH;
    }

    public DefaultRandomStringGenerator(final int maxRandomLength) {
        this.maximumRandomLength = maxRandomLength;
    }

    public int getMinLength() {
        return this.maximumRandomLength;
    }

    public int getMaxLength() {
        return this.maximumRandomLength;
    }

    public String getNewString() {
        final byte[] random = getNewStringAsBytes();

        return convertBytesToString(random);
    }


    public byte[] getNewStringAsBytes() {
        final byte[] random = new byte[this.maximumRandomLength];

        this.randomizer.nextBytes(random);
        
        return random;
    }

    private String convertBytesToString(final byte[] random) {
        final char[] output = new char[random.length];
        for (int i = 0; i < random.length; i++) {
            final int index = Math.abs(random[i] % PRINTABLE_CHARACTERS.length);
            output[i] = PRINTABLE_CHARACTERS[index];
        }

        return new String(output);
    }
}
