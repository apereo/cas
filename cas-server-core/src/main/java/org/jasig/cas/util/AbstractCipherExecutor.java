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

import javax.validation.constraints.NotNull;

/**
 * Abstract cipher to provide common operations around signing objects.
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCipherExecutor<T, R> implements CipherExecutor<T, R> {
    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AesKey signingKey;

    /**
     * Instantiates a new cipher executor.
     *
     */
    protected AbstractCipherExecutor() {}

    /**
     * Instantiates a new cipher executor.
     *
     * @param signingSecretKey the signing key
     */
    public AbstractCipherExecutor(final String signingSecretKey) {
        setSigningKey(signingSecretKey);
    }

    public void setSigningKey(final String signingSecretKey) {
        this.signingKey = new AesKey(signingSecretKey.getBytes());
    }

    /**
     * Sign the array by first turning it into a base64 encoded string.
     *
     * @param value the value
     * @return the byte [ ]
     */
    protected byte[] sign(final byte[] value) {
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
    protected byte[] verifySignature(@NotNull final byte[] value) {
        try {
            final String asString = new String(value);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(asString);
            jws.setKey(this.signingKey);

            final boolean verified = jws.verifySignature();
            if (verified) {
                final String payload = jws.getPayload();
                logger.debug("Successfully decoded value. Result in Base64-encoding is [{}]", payload);
                return CompressionUtils.decodeBase64ToByteArray(payload);
            }
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }


}
