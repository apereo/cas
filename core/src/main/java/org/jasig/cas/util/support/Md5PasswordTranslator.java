/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jasig.cas.util.PasswordTranslator;

/**
 * Implementation of the password handler that returns the MD5 hash of any
 * plaintext password passed into the translator. Returns the equivalent Md5
 * Hash you would get from a PHP hash.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Md5PasswordTranslator implements PasswordTranslator {

    /** The base we will use to convert the Integer to a String. */
    private static final int BASE = 16;

    /** The name of the algorithm to use. */
    private static final String ALGORITHM_NAME = "MD5";

    public String translate(final String password) {

        if (password == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest
                .getInstance(ALGORITHM_NAME);

            return new BigInteger(messageDigest.digest(password.getBytes()))
                .toString(BASE);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }
}
