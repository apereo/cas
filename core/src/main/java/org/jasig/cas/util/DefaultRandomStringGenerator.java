/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.security.SecureRandom;

/**
 * Implementation of the RandomStringGenerator that allows you to define the length of the random part.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultRandomStringGenerator implements RandomStringGenerator {

    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679"
        .toCharArray();

    private SecureRandom randomizer = new SecureRandom();

    private final int MAX_RANDOM_LENGTH;

    public DefaultRandomStringGenerator() {
        this.MAX_RANDOM_LENGTH = 35;
    }

    public DefaultRandomStringGenerator(final int maxRandomLength) {
        this.MAX_RANDOM_LENGTH = maxRandomLength;
    }

    public int getMinLength() {
        return this.MAX_RANDOM_LENGTH;
    }

    public int getMaxLength() {
        return this.MAX_RANDOM_LENGTH;
    }

    public synchronized String getNewString() {
        final byte[] random = new byte[this.MAX_RANDOM_LENGTH];

        this.randomizer.nextBytes(random);

        return convertBytesToString(random);
    }

    private String convertBytesToString(byte[] random) {
        final char[] output = new char[random.length];
        for (int i = 0; i < random.length; i++) {
            final int index = Math.abs(random[i] % PRINTABLE_CHARACTERS.length);
            output[i] = PRINTABLE_CHARACTERS[index];
        }

        return new String(output);
    }
}