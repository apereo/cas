/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jasig.cas.util.PasswordTranslator;

/**
 * Implementation of the password handler that returns the MD5 hash of any plaintext password passed into the translator.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class Md5PasswordTranslator implements PasswordTranslator {

    /**
     * @see org.jasig.cas.util.PasswordTranslator#translate(java.lang.String)
     */
    public String translate(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            return new String(messageDigest.digest());
        }
        catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e.getMessage());
        }
    }

}
