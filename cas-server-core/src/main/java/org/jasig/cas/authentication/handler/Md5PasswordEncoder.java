/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

/**
 * Implementation of the password handler that returns the MD5 hash of any
 * plaintext password passed into the encoder. Returns the equivalent Md5 Hash
 * you would get from a PHP hash.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Md5PasswordEncoder extends AbstractPasswordEncoder {

    protected String getEncodingAlgorithm() {
        return "MD5";
    }

    protected String getFormattedText(byte[] digest) {
        final StringBuilder hexString = new StringBuilder(digest.length * 2);

        for (int i = 0; i < digest.length; i++) {
            final String plainText = Integer.toHexString(0xFF & digest[i]);

            if (plainText.length() < 2) {
                hexString.append("0");
            }
            hexString.append(plainText);
        }

        return hexString.toString();
    }
}
