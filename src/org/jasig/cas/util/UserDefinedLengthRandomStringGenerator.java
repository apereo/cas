/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.security.SecureRandom;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class UserDefinedLengthRandomStringGenerator implements RandomStringGenerator {
    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679".toCharArray();
    private static final int MAX_TIMESTAMP_LENGTH = 10;

    private static final int DEFAULT_BASE = 36;

    private static final int MAX_ALPHANUMERIC_VALUE_LENGTH = Integer.toString(Integer.MAX_VALUE, DEFAULT_BASE).length();

    private static final int DEFAULT_LENGTH = 15;

    private static final long MAX_TIMESTAMP_LEN = 3656158440062976L; // DEFAULT_BASE ^ MAX_TIMESTAMP_LENGTH

    private long lastTimeUsed = 0;

    private long countForLastTimeUsed = 0;

    private SecureRandom randomizer = new SecureRandom();

    private final int MAX_RANDOM_LENGTH;
    
    public UserDefinedLengthRandomStringGenerator(final int maxRandomLength) {
        this.MAX_RANDOM_LENGTH = maxRandomLength;
    }

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getMinLength()
     */
    public int getMinLength() {
        return MAX_TIMESTAMP_LENGTH + this.MAX_RANDOM_LENGTH + 1;
    }

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getMaxLength()
     */
    public int getMaxLength() {
        return MAX_TIMESTAMP_LENGTH + this.MAX_RANDOM_LENGTH + MAX_ALPHANUMERIC_VALUE_LENGTH;
    }

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getNewString()
     */
    public synchronized String getNewString() {
        long currentTime = System.currentTimeMillis();
        long count = 0;
        final byte[] random = new byte[this.MAX_RANDOM_LENGTH];
        StringBuffer buffer = new StringBuffer(DEFAULT_LENGTH);
        
        this.randomizer.nextBytes(random);

        if (currentTime == this.lastTimeUsed)
            count = this.countForLastTimeUsed++;
        else {
            this.lastTimeUsed = currentTime;
            count = 0;
        }

        currentTime %= MAX_TIMESTAMP_LEN;
        currentTime += MAX_TIMESTAMP_LEN;

        buffer.append(convertBytesToString(random));
        buffer.append(Long.toString(currentTime, DEFAULT_BASE).substring(1));
        buffer.append(Long.toString(count, DEFAULT_BASE));

        return buffer.toString();
    }
    
    private String convertBytesToString(byte[] random) {
        char[] output = new char[random.length];
        for (int i = 0; i < random.length; i++) {
            final int index = Math.abs(random[i] % PRINTABLE_CHARACTERS.length);
            output[i] = PRINTABLE_CHARACTERS[index];
        }
        
        return new String(output);
    }
}
