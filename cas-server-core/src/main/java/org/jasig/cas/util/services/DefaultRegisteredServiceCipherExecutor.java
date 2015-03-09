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

package org.jasig.cas.util.services;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.PublicKey;

/**
 * Default cipher implementation based on public keys.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultRegisteredServiceCipherExecutor implements RegisteredServiceCipherExecutor {
    private static final String UTF8_ENCODING = "UTF-8";

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Encrypt using the given cipher associated with the service,
     * and encode the data in base 64.
     *
     * @param data the data
     * @return the encoded piece of data in base64
     */
    @Override
    public final String encode(final String data, final RegisteredService service) {
        try {
            final PublicKey publicKey = createRegisteredServicePublicKey(service);
            final byte[] result = encodeInternal(data, publicKey, service);
            if (result != null) {
                return CompressionUtils.encodeBase64(result);
            }
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Encode internally, meant to be called by extensions.
     * Default behavior will encode the data based on the
     * registered service public key's algorithm using {@link javax.crypto.Cipher}.
     *
     * @param data the data
     * @param publicKey the public key
     * @param registeredService the registered service
     * @return a byte[] that contains the encrypted result
     */
    protected byte[] encodeInternal(final String data, final PublicKey publicKey,
                                    final RegisteredService registeredService) {
        try {
            final Cipher cipher = initializeCipherBasedOnServicePublicKey(publicKey, registeredService);
            if (cipher != null) {
                return cipher.doFinal(data.getBytes(UTF8_ENCODING));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Create registered service public key defined.
     *
     * @param registeredService the registered service
     * @return the public key
     * @throws Exception the exception, if key cant be created
     */
    private PublicKey createRegisteredServicePublicKey(final RegisteredService registeredService) throws Exception {
        if (registeredService.getPublicKey() == null) {
            logger.debug("No public key is defined for service [{}]. No encoding will take place.",
                    registeredService);
            return null;
        }
        final PublicKey publicKey = registeredService.getPublicKey().createInstance();
        if (publicKey == null) {
            logger.debug("No public key instance created for service [{}]. No encoding will take place.",
                    registeredService);
            return null;
        }
        return publicKey;
    }

    /**
     * Initialize cipher based on service public key.
     *
     * @param publicKey the public key
     * @param registeredService the registered service
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    private Cipher initializeCipherBasedOnServicePublicKey(final PublicKey publicKey,
                                                           final RegisteredService registeredService) {
        try {
            logger.debug("Using public key [{}] to initialize the cipher",
                    registeredService.getPublicKey());

            final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            logger.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}]",
                    publicKey.getAlgorithm());
            return cipher;
        } catch (final Exception e) {
            logger.warn("Cipher could not be initialized for service [{}]. Error [{}]",
                    registeredService, e.getMessage());
        }
        return null;
    }
}
