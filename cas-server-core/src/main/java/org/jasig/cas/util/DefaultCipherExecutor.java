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
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link org.jasig.cas.util.DefaultCipherExecutor} is the default
 * implementation of {@link org.jasig.cas.util.CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class DefaultCipherExecutor implements CipherExecutor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String contentEncryptionAlgorithmIdentifier;

    private final String signingAlgorithm;

    private final Key secretKeyEncryptionKey;

    private final Key secretKeySigningKey;

    /**
     * Instantiates a new cipher.
     *
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     */
    public DefaultCipherExecutor(final String secretKeyEncryption,
                                 final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning,
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
                AlgorithmIdentifiers.HMAC_SHA512);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption the key for encryption
     * @param secretKeySigning the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     * @param signingAlgorithm the signing algorithm
     */
    public DefaultCipherExecutor(final String secretKeyEncryption,
                                 final String secretKeySigning,
                                 final String contentEncryptionAlgorithmIdentifier,
                                 final String signingAlgorithm) {
        this.secretKeyEncryptionKey =  prepareJsonWebTokenKey(secretKeyEncryption);
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;

        logger.debug("Initialized cipher encryption sequence via [{}]",
                 contentEncryptionAlgorithmIdentifier);

        this.signingAlgorithm = signingAlgorithm;
        this.secretKeySigningKey = new AesKey(secretKeySigning.getBytes());

        logger.debug("Initialized cipher signing sequence via [{}]",
                signingAlgorithm);

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
     * Prepare json web token key.
     *
     * @param secret the secret
     * @return the key
     */
    private Key prepareJsonWebTokenKey(final String secret) {
        try {
            final Map<String, Object> keys = new HashMap<>(2);
            keys.put("kty", "oct");
            keys.put("k", secret);
            final JsonWebKey jwk = JsonWebKey.Factory.newJwk(keys);
            return jwk.getKey();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(value);
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
            jwe.setEncryptionMethodHeaderParameter(this.contentEncryptionAlgorithmIdentifier);
            jwe.setKey(this.secretKeyEncryptionKey);
            logger.debug("Encrypting via [{}]", this.contentEncryptionAlgorithmIdentifier);
            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files. "
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
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(this.secretKeyEncryptionKey);
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
            jws.setKey(this.secretKeySigningKey);
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
            jws.setKey(this.secretKeySigningKey);
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
