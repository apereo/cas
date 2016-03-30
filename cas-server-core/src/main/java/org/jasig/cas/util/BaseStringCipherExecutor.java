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
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;

import javax.validation.constraints.NotNull;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link BaseStringCipherExecutor} is the default
 * implementation of {@link CipherExecutor}. It provides
 * a facade API to encrypt, sign, and verify values.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class BaseStringCipherExecutor extends AbstractCipherExecutor<String, String> {
    private static final int ENCRYPTION_KEY_SIZE = 256;

    private static final int SIGNING_KEY_SIZE = 512;

    private String contentEncryptionAlgorithmIdentifier;

    private Key secretKeyEncryptionKey;

    /**
     * Instantiates a new Base string cipher executor.
     */
    private BaseStringCipherExecutor() {}

    /**
     * Instantiates a new cipher.
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     *
     * @param secretKeyEncryption the secret key encryption; must be represented as a octet sequence JSON Web Key (JWK)
     * @param secretKeySigning    the secret key signing; must be represented as a octet sequence JSON Web Key (JWK)
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning) {
        this(secretKeyEncryption, secretKeySigning,
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
    }

    /**
     * Instantiates a new cipher.
     *
     * @param secretKeyEncryption                  the key for encryption
     * @param secretKeySigning                     the key for signing
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public BaseStringCipherExecutor(final String secretKeyEncryption,
                                    final String secretKeySigning,
                                    final String contentEncryptionAlgorithmIdentifier) {

        super();

        if (StringUtils.isBlank(contentEncryptionAlgorithmIdentifier)) {
            logger.debug("contentEncryptionAlgorithmIdentifier is not defined");
            return;
        }

        String secretKeyToUse = secretKeyEncryption;
        if (StringUtils.isBlank(secretKeyToUse)) {
            logger.warn("Secret key for encryption is not defined. CAS will attempt to auto-generate the encryption key");
            secretKeyToUse = generateOctetJsonWebKeyOfSize(ENCRYPTION_KEY_SIZE);
            logger.warn("Generated encryption key {} of size {}. The generated key MUST be added to CAS settings.",
                    secretKeyToUse, ENCRYPTION_KEY_SIZE);
        }

        String signingKeyToUse = secretKeySigning;
        if (StringUtils.isBlank(signingKeyToUse)) {
            logger.warn("Secret key for signing is not defined. CAS will attempt to auto-generate the signing key");
            signingKeyToUse = generateOctetJsonWebKeyOfSize(SIGNING_KEY_SIZE);
            logger.warn("Generated signing key {} of size {}. The generated key MUST be added to CAS settings.",
                    signingKeyToUse, SIGNING_KEY_SIZE);
        }


        setSigningKey(signingKeyToUse);
        this.secretKeyEncryptionKey = prepareJsonWebTokenKey(secretKeyToUse);
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;

        logger.debug("Initialized cipher encryption sequence via [{}]",
                contentEncryptionAlgorithmIdentifier);

    }

    @Override
    public String encode(final String value) {
        final String encoded = encryptValue(value);
        final String signed = new String(sign(encoded.getBytes()));
        return signed;
    }

    @Override
    public String decode(final String value) {
        final byte[] encoded = verifySignature(value.getBytes());
        if (encoded != null && encoded.length > 0) {
            return decryptValue(new String(encoded));
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
     * Encrypt the value based on the seed array whose length was given during afterPropertiesSet,
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
     * Decrypt value based on the key created during afterPropertiesSet.
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
     * Generate octet json web key of size string.
     *
     * @param size the size
     * @return the string
     */
    private String generateOctetJsonWebKeyOfSize(final int size) {
        final OctetSequenceJsonWebKey octetKey = OctJwkGenerator.generateJwk(size);
        final Map<String, Object> params = octetKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        return params.get("k").toString();
    }
}
