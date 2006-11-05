/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of the password handler that returns the MD5 hash of any
 * plaintext password passed into the encoder. Returns the equivalent Md5 Hash
 * you would get from a PHP hash.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Md5PasswordEncoder implements PasswordEncoder {

    /** The name of the algorithm to use. */
    private static final String ALGORITHM_NAME = "MD5";

    /**
     * @throws SecurityException if the Algorithm can't be found.
     */
    public String encode(final String password) {

        if (password == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest
                .getInstance(ALGORITHM_NAME);
            messageDigest.update(password.getBytes());

            final byte[] digest = messageDigest.digest();
            StringBuffer hexString = new StringBuffer();

            synchronized (hexString) {
                for (int i = 0; i < digest.length; i++) {
                    final String plainText = Integer
                        .toHexString(0xFF & digest[i]);

                    if (plainText.length() < 2) {
                        hexString.append("0");
                    }

                    hexString.append(plainText);
                }

            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }
}
