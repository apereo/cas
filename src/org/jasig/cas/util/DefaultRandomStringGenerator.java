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
public class DefaultRandomStringGenerator implements RandomStringGenerator {

    private static final int MAX_TIMESTAMP_LENGTH = 10;

    private static final int MAX_RANDOM_LENGTH = 12;

    private static final int MIN_LENGTH = MAX_TIMESTAMP_LENGTH + MAX_RANDOM_LENGTH + 1;

    private static final int DEFAULT_BASE = 36;

    private static final int MAX_ALPHANUMERIC_VALUE_LENGTH = Integer.toString(Integer.MAX_VALUE, DEFAULT_BASE).length();

    private static final int MAX_LENGTH = MAX_TIMESTAMP_LENGTH + MAX_RANDOM_LENGTH + MAX_ALPHANUMERIC_VALUE_LENGTH;

    private static final int DEFAULT_LENGTH = 15;

    private static final long MAX_RANDOM_LEN = 4738381338321616896L; // DEFAULT_BASE ^ MAX_RANDOM_LENGTH

    private static final long MAX_TIMESTAMP_LEN = 3656158440062976L; // DEFAULT_BASE ^ MAX_TIMESTAMP_LENGTH

    private long lastTimeUsed = 0;

    private long countForLastTimeUsed = 0;

    private SecureRandom randomizer = new SecureRandom();;

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getMinLength()
     */
    public int getMinLength() {
        return MIN_LENGTH;
    }

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getMaxLength()
     */
    public int getMaxLength() {
        return MAX_LENGTH;
    }

    /**
     * @see org.jasig.cas.util.RandomStringGenerator#getNewString()
     */
    public synchronized String getNewString() {
        long currentTime = System.currentTimeMillis();
        long count = 0;
        long random = Math.abs(this.randomizer.nextLong());
        long random2 = Math.abs(this.randomizer.nextLong());
        StringBuffer buffer = new StringBuffer(DEFAULT_LENGTH);

        if (currentTime == this.lastTimeUsed)
            count = this.countForLastTimeUsed++;
        else {
            this.lastTimeUsed = currentTime;
            count = 0;
        }

        currentTime %= MAX_TIMESTAMP_LEN;
        currentTime += MAX_TIMESTAMP_LEN;

        random %= MAX_RANDOM_LEN;
        random += MAX_RANDOM_LEN;
        
        random2 %= MAX_RANDOM_LEN;
        random2 += MAX_RANDOM_LEN;

        buffer.append(Long.toString(random, DEFAULT_BASE).substring(1));
        buffer.append(Long.toString(currentTime, DEFAULT_BASE).substring(1));
        buffer.append(Long.toString(count, DEFAULT_BASE));
        buffer.append(Long.toString(random2, DEFAULT_BASE).substring(1));

        return buffer.toString();
    }
}
