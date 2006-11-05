/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of the password handler that returns the SHA1 hash of any
 * plaintext password passed into the encoder. Returns the equivalent SHA1 Hash
 * you would get from a Perl digest.
 * 
 * @author Stephen More
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class Sha1PasswordEncoder implements PasswordEncoder {

    /** The name of the algorithm to use. */
    private static final String ALGORITHM_NAME = "SHA1";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * @throws SecurityException if the Algorithm can't be found.
     */
    public String encode(final String password) {
        if (password == null) {
            return null;
        }

        try {
            final MessageDigest messageDigest = MessageDigest
                .getInstance(ALGORITHM_NAME);

            messageDigest.update(password.getBytes());

            return bytesToHex(messageDigest.digest());
        } catch (final NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }

    private String bytesToHex(final byte[] b) {
        final StringBuffer buf = new StringBuffer();

        synchronized (buf) {
            for (int j = 0; j < b.length; j++) {
                buf.append(HEX_DIGITS[(b[j] >> 4) & 0x0f]);
                buf.append(HEX_DIGITS[b[j] & 0x0f]);
            }
        }
        return buf.toString();
    }
}
