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

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.security.Key;

/**
 * The {@link org.jasig.cas.util.DefaultCipherExecutor} is the default
 * implementation of {@link org.jasig.cas.util.CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class DefaultCipherExecutor implements CipherExecutor {
    private static final int DEFAULT_ENCRYPTION_KEY_LENGTH = 32;

    private static final int DEFAULT_SIGNING_KEY_LENGTH = 64;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[] encryptionSeedArray;

    private final byte[] signingSeedArray;

    private final String keyManagementAlgorithmIdentifier;

    private final String contentEncryptionAlgorithmIdentifier;

    private final String signingAlgorithm;

    /**
     * Instantiates a new cipher
     * Uses an encryption key length of {@link #DEFAULT_ENCRYPTION_KEY_LENGTH}
     * with {@link KeyManagementAlgorithmIdentifiers#A256KW} as the key
     * and {@link ContentEncryptionAlgorithmIdentifiers#AES_256_CBC_HMAC_SHA_512}
     * as the content encryption.
     *
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     */
    public DefaultCipherExecutor() {
        this(DEFAULT_ENCRYPTION_KEY_LENGTH, DEFAULT_SIGNING_KEY_LENGTH,
                KeyManagementAlgorithmIdentifiers.A256KW,
                ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512,
                AlgorithmIdentifiers.HMAC_SHA512);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param keyLength the key length
     * @param signingKeyLength the signing key length
     * @param keyManagementAlgorithmIdentifier the key management algorithm identifier
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     * @param signingAlgorithm the signing algorithm
     */
    public DefaultCipherExecutor(final int keyLength,
                                 final int signingKeyLength,
                                 final String keyManagementAlgorithmIdentifier,
                                 final String contentEncryptionAlgorithmIdentifier,
                                 final String signingAlgorithm) {
        this.encryptionSeedArray = ByteUtil.randomBytes(keyLength);
        this.keyManagementAlgorithmIdentifier = keyManagementAlgorithmIdentifier;
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;

        logger.debug("Initialized cipher encryption sequence via [{}] and [{}]",
                keyManagementAlgorithmIdentifier, contentEncryptionAlgorithmIdentifier);

        this.signingAlgorithm = signingAlgorithm;
        this.signingSeedArray = ByteUtil.randomBytes(signingKeyLength);

        logger.debug("Initialized cipher signing sequence via [{}] and key length [{}]",
                signingAlgorithm, signingKeyLength);
    }

    @Override
    public String encode(final String value) {
        final String encoded = encryptValue(value);
        return signValue(encoded);
    }

    @Override
    public String decode(final String value) {
        final String encoded = verifySignature(value);
        if (StringUtils.isNotBlank(encoded)) {
            return decryptValue(encoded);
        }
        return null;
    }

    /**
     * Encrypt the value based on the seed array whose length was given during init,
     * and the key and content encryption ids.
     *
     * @param value the value
     * @return the encoded value
     */
    private String encryptValue(@NotNull final String value) {
        try {
            final Key key = new AesKey(this.encryptionSeedArray);
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(value);
            jwe.setAlgorithmHeaderValue(this.keyManagementAlgorithmIdentifier);
            jwe.setEncryptionMethodHeaderParameter(this.contentEncryptionAlgorithmIdentifier);
            jwe.setKey(key);

            logger.debug("Encrypting via [{}] and [{}]", this.keyManagementAlgorithmIdentifier,
                    this.contentEncryptionAlgorithmIdentifier);

            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files."
                    + e.getMessage(), e);
        }
    }

    /**
     * Decrypt value based on the key created during init.
     *
     * @param value the value
     * @return the decrypted value
     */
    private String decryptValue(@NotNull final String value) {
        try {
            final Key key = new AesKey(this.encryptionSeedArray);
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(key);
            jwe.setCompactSerialization(value);
            logger.debug("Decrypting value...");
            return jwe.getPayload();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs value based on the signing algorithm and the key length.
     *
     * @param value the value
     * @return the signed value
     */
    private String signValue(@NotNull final String value) {
        try {
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(value);
            jws.setAlgorithmHeaderValue(this.signingAlgorithm);
            jws.setKey(new AesKey(this.signingSeedArray));
            return jws.getCompactSerialization();
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
    private String verifySignature(@NotNull final String value) {
        try {
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(value);
            jws.setKey(new AesKey(this.signingSeedArray));
            final boolean verified = jws.verifySignature();
            if (verified) {
                logger.debug("Signature successfully verified. Payload is [{}]", jws.getPayload());
                return jws.getPayload();
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
