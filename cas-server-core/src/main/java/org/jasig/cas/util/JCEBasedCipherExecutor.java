/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;

/**
 * A {@link CipherExecutor} implementation that is based on algorithms
 * provided by the default platform's JCE. By default AES encryption is
 * used which requires both the secret key and the IV length to be of size 16.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class JCEBasedCipherExecutor implements CipherExecutor<byte[], byte[]> {
    private static final String UTF8_ENCODING = "UTF-8";

    /** Cipher algorithm. Default is <code>AES/CBC/NoPadding</code>. */
    private String cipherAlgorithm = "AES/CBC/PKCS5Padding";

    /** Secret key IV algorithm. Default is <code>AES</code>. */
    private String secretKeyAlgorithm = "AES";

    private final String encryptionSecretKey;
    private final String encryptionSeed;

    /**
     * Instantiates a new cryptic ticket cipher executor.
     *
     * @param encryptionSecretKey the encryption secret key
     * @param encryptionSeed the encryption seed
     */
    public JCEBasedCipherExecutor(final String encryptionSecretKey, final String encryptionSeed) {
        this.encryptionSecretKey = encryptionSecretKey;
        this.encryptionSeed = encryptionSeed;
    }

    public void setCipherAlgorithm(final String cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public void setSecretKeyAlgorithm(final String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    public byte[] encode(@NotNull final byte[] value) {
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            final SecretKeySpec key = new SecretKeySpec(this.encryptionSecretKey.getBytes(),
                    this.secretKeyAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(
                    this.encryptionSeed.getBytes()));
            final byte[] result = cipher.doFinal(value);
            return result;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decode(@NotNull final byte[] value) {
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            final SecretKeySpec key = new SecretKeySpec(this.encryptionSecretKey.getBytes(UTF8_ENCODING),
                    this.secretKeyAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(
                    this.encryptionSeed.getBytes(UTF8_ENCODING)));

            final byte[] result = cipher.doFinal(value);
            return result;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
