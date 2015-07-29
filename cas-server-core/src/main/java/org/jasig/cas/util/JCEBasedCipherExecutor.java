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

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Cipher algorithm. Default is <code>AES/CBC/NoPadding</code>. */
    private String cipherAlgorithm = "AES/CBC/PKCS5Padding";

    /** Secret key IV algorithm. Default is <code>AES</code>. */
    private String secretKeyAlgorithm = "AES";

    private final String encryptionSecretKey;
    private final String encryptionSeed;
    private final AesKey signingKey;

    /**
     * Instantiates a new cryptic ticket cipher executor.
     *
     * @param encryptionSecretKey the encryption secret key
     * @param encryptionSeed the encryption seed
     * @param signingSecretKey the signing key
     */
    public JCEBasedCipherExecutor(final String encryptionSecretKey,
                                  final String encryptionSeed,
                                  final String signingSecretKey) {
        this.encryptionSecretKey = encryptionSecretKey;
        this.encryptionSeed = encryptionSeed;
        this.signingKey = new AesKey(signingSecretKey.getBytes());
    }

    public void setCipherAlgorithm(final String cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public void setSecretKeyAlgorithm(final String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    public byte[] encode(final byte[] value) {
        try {
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            final SecretKeySpec key = new SecretKeySpec(this.encryptionSecretKey.getBytes(),
                    this.secretKeyAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(
                    this.encryptionSeed.getBytes()));
            final byte[] result = cipher.doFinal(value);
            return sign(result);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decode(final byte[] value) {
        try {
            final byte[] verifiedValue = verifySignature(value);
            final Cipher cipher = Cipher.getInstance(this.cipherAlgorithm);
            final SecretKeySpec key = new SecretKeySpec(this.encryptionSecretKey.getBytes(UTF8_ENCODING),
                    this.secretKeyAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(
                    this.encryptionSeed.getBytes(UTF8_ENCODING)));

            final byte[] result = cipher.doFinal(verifiedValue);
            return result;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value the value
     * @return the byte [ ]
     */
    private byte[] sign(final byte[] value) {
        try {
            final String base64 = CompressionUtils.encodeBase64(value);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(base64);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
            jws.setKey(this.signingKey);
            return jws.getCompactSerialization().getBytes();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verify signature.
     *
     * @param value the value
     * @return the value associated with the signature, which may have to
     * be decoded, or null.
     */
    private byte[] verifySignature(@NotNull final byte[] value) {
        try {
            final String asString = new String(value);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(asString);
            jws.setKey(this.signingKey);

            final boolean verified = jws.verifySignature();
            if (verified) {
                final String payload = jws.getPayload();
                logger.debug("Successfully decoded value. Result in Base64-encoding is [{}]", payload);
                return CompressionUtils.decodeBase64(payload);
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
