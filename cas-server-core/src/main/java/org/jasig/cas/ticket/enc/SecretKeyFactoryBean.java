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

package org.jasig.cas.ticket.enc;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Factory bean that creates {@link SecretKey} objects from a file containing
 * key material.
 *
 * @author Marvin S. Addison
 * @since 4.1
 *
 */
public final class SecretKeyFactoryBean extends AbstractFactoryBean<SecretKey> {
    /** Default cipher is AES. */
    private static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";

    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    /** Cipher name for which key was created. */
    @NotNull
    private String cipher = DEFAULT_ENCRYPTION_ALGORITHM;

    private final String salt;

    private final String secretKey;

    /**
     * Instantiates a new Secret key factory bean
     * with the default cipher algorithm as
     * {@link #DEFAULT_ENCRYPTION_ALGORITHM}.
     */
    public SecretKeyFactoryBean() {
        this(DEFAULT_ENCRYPTION_ALGORITHM, getRandomSalt(16), getRandomSalt(16));
    }

    /**
     * Instantiates a new Secret key factory bean.
     *
     * @param cipher the cipher
     * @param salt the salt
     * @param secretKey the secret key
     */
    public SecretKeyFactoryBean(final String cipher, final String salt, final String secretKey) {
        this.cipher = cipher;
        this.salt = salt;
        this.secretKey = secretKey;
    }

    /** {@inheritDoc} */
    @Override
    protected SecretKey createInstance() throws Exception {
        return getSecretKey();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getObjectType() {
        return SecretKey.class;
    }

    /**
    * Gets the secret key.
    *
    * @return the secret key
    * @throws Exception the exception
    */
    private SecretKey getSecretKey() throws Exception {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        final KeySpec spec = new PBEKeySpec(this.secretKey.toCharArray(), this.salt.getBytes("UTF-8"), 65536, 128);
        final SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), this.cipher);
    }

    /**
     * Gets the random salt.
     *
     * @param size the size
     * @return the random salt
     */
    private static String getRandomSalt(final int size) {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return getFormattedText(bytes);
    }

    /**
     * Takes the raw bytes from the digest and formats them correct.
     *
     * @param bytes the raw bytes from the digest.
     * @return the formatted bytes.
     */
    private static String getFormattedText(final byte[] bytes) {
        final StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            buf.append(HEX_DIGITS[b >> 4 & 0x0f]);
            buf.append(HEX_DIGITS[b & 0x0f]);
        }
        return buf.toString();
    }

}
