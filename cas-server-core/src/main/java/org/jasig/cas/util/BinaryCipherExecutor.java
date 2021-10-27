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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.crypto.CipherService;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Map;

/**
 * A implementation that is based on algorithms
 * provided by the default platform's JCE. By default AES encryption is
 * used which requires both the secret key and the IV length to be of size 16.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class BinaryCipherExecutor extends AbstractCipherExecutor<byte[], byte[]> {
    private static final String UTF8_ENCODING = "UTF-8";

    private static final int SIGNING_KEY_SIZE = 512;

    private static final int ENCRYPTION_KEY_SIZE = 16;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Secret key IV algorithm. Default is {@code AES}. */
    private String secretKeyAlgorithm = "AES";

    private String encryptionSecretKey;


    /**
     * Instantiates a new cryptic ticket cipher executor.
     *
     * @param encryptionSecretKey the encryption secret key
     * @param signingSecretKey the signing key
     */
    public BinaryCipherExecutor(final String encryptionSecretKey,
                                final String signingSecretKey) {
        verifyAndSetKeys(encryptionSecretKey, signingSecretKey);
    }

    /**
     * Verify and set keys.
     *
     * @param encryptionSecretKey the encryption secret key
     * @param signingSecretKey    the signing secret key
     */
    private void verifyAndSetKeys(final String encryptionSecretKey, final String signingSecretKey) {

        String signingKeyToUse = signingSecretKey;
        if (StringUtils.isBlank(signingKeyToUse)) {
            logger.warn("Secret key for signing is not defined. CAS will attempt to auto-generate the signing key");
            signingKeyToUse = generateOctetJsonWebKeyOfSize(SIGNING_KEY_SIZE);
            logger.warn("Generated signing key {} of size {}. The generated key MUST be added to CAS settings.",
                    signingKeyToUse, SIGNING_KEY_SIZE);
        }
        setSigningKey(signingKeyToUse);

        if (StringUtils.isBlank(encryptionSecretKey)) {
            logger.warn("No encryption key is defined. CAS will attempt to auto-generate keys");
            this.encryptionSecretKey = RandomStringUtils.randomAlphabetic(ENCRYPTION_KEY_SIZE);
            logger.warn("Generated encryption key {} of size {}. The generated key MUST be added to CAS settings.",
                    this.encryptionSecretKey, ENCRYPTION_KEY_SIZE);
        } else {
            this.encryptionSecretKey = encryptionSecretKey;
        }
    }

    public void setSecretKeyAlgorithm(final String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    public byte[] encode(final byte[] value) {
        try {
            final Key key = new SecretKeySpec(this.encryptionSecretKey.getBytes(),
                    this.secretKeyAlgorithm);
            final CipherService cipher = new AesCipherService();
            final byte[] result = cipher.encrypt(value, key.getEncoded()).getBytes();
            return sign(result);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decode(final byte[] value) {
        try {
            final byte[] verifiedValue = verifySignature(value);
            final Key key = new SecretKeySpec(this.encryptionSecretKey.getBytes(UTF8_ENCODING),
                    this.secretKeyAlgorithm);
            final CipherService cipher = new AesCipherService();
            final byte[] result = cipher.decrypt(verifiedValue, key.getEncoded()).getBytes();
            return result;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
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
