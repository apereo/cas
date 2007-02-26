/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractPasswordEncoder implements PasswordEncoder {

    public final String encode(final String password) {
        if (password == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest
                .getInstance(getEncodingAlgorithm());
            messageDigest.update(password.getBytes());

            final byte[] digest = messageDigest.digest();

            return getFormattedText(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Returns the Encoding algorithm to use.
     * 
     * @return the encoding algorithm
     */
    protected abstract String getEncodingAlgorithm();

    /**
     * Takes the raw bytes from the digest and formats them correct.
     * 
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    protected abstract String getFormattedText(final byte[] bytes);
}
