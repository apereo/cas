/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication.handler;

import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of PasswordEncoder using message digest. Can accept any
 * message digest that the JDK can accept, including MD5 and SHA1. Returns the
 * equivalent Hash you would get from a Perl digest.
 * 
 * @author Scott Battaglia
 * @author Stephen More
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DefaultPasswordEncoder implements PasswordEncoder {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @NotNull
    private final String encodingAlgorithm;

    private String characterEncoding;

    public DefaultPasswordEncoder(final String encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }

    public String encode(final String password) {
        if (password == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest
                .getInstance(this.encodingAlgorithm);

            if (StringUtils.hasText(this.characterEncoding)) {
                messageDigest.update(password.getBytes(this.characterEncoding));
            } else {
                messageDigest.update(password.getBytes());
            }


            final byte[] digest = messageDigest.digest();

            return getFormattedText(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Takes the raw bytes from the digest and formats them correct.
     * 
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    private String getFormattedText(byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (int j = 0; j < bytes.length; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public final void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }
}
