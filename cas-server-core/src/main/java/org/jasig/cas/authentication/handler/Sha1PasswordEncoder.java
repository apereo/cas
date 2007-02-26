/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

/**
 * Implementation of the password handler that returns the SHA1 hash of any
 * plaintext password passed into the encoder. Returns the equivalent SHA1 Hash
 * you would get from a Perl digest.
 * 
 * @author Stephen More
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class Sha1PasswordEncoder extends AbstractPasswordEncoder {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected String getEncodingAlgorithm() {
        return "SHA1";
    }

    protected String getFormattedText(byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int j = 0; j < bytes.length; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }
}
