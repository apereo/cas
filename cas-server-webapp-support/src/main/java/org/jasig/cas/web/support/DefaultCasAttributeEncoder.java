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

package org.jasig.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of the attribute
 * encoder that will use a per-service key-pair
 * to encrypt the credential password and PGT
 * when available. All other attributes remain in
 * place.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultCasAttributeEncoder implements CasAttributeEncoder {

    /** The Cipher instance used to encrypt attributes. */
    protected final Cipher cipher;

    /** The Services manager. */
    @NotNull
    protected final ServicesManager servicesManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String cachedCredential;

    /**
     * Instantiates a new Cache credentials meta data populator.
     * The default cipher algorithm is set to be
     * {@link #DEFAULT_CIPHER_ALGORITHM}.
     * @param servicesManager the services manager
     * @throws java.lang.Exception in case the cipher cannot be initialized
     */
    public DefaultCasAttributeEncoder(final ServicesManager servicesManager) throws Exception {
        this(DEFAULT_CIPHER_ALGORITHM, servicesManager);
    }

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param cipherAlgorithm the cipher algorithm
     * @param servicesManager the services manager
     * @throws java.lang.Exception in case the cipher cannot be initialized
     */
    public DefaultCasAttributeEncoder(final String cipherAlgorithm, final ServicesManager servicesManager)
            throws Exception {
        this.servicesManager = servicesManager;
        this.cipher = Cipher.getInstance(cipherAlgorithm);
        logger.debug("Created cipher instance to encrypt credential via [{}]", cipherAlgorithm);
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes the attributes
     */
    protected void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes) {
        try {
            if (StringUtils.isNotBlank(this.cachedCredential)) {
                logger.debug("Retrieved the password as an authentication attribute...");
                final byte[] cipherData = this.cipher.doFinal(this.cachedCredential.getBytes());
                final String encPassword = CompressionUtils.encodeBase64(cipherData);

                attributes.put(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, encPassword);
                logger.debug("Encrypted and encoded password as an authentication attribute.");
            } else {
                logger.debug("Credential is not available as an authentication attribute to encrypt...");
            }

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the cipher with the public key
     * and then start to encrypt select attributes.
     *
     * @param attributes the attributes
     * @param service the service
     */
    protected void encodeAttributesInternal(final Map<String, Object> attributes, final RegisteredService service) {
        try {
            if (!initializeCipherBasedOnServicePublicKey(service)) {
                return;
            }

            encodeAndEncryptCredentialPassword(attributes);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize cipher based on service public key.
     *
     * @param service the service
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    protected boolean initializeCipherBasedOnServicePublicKey(final RegisteredService service) {
        try {
            final PublicKey publicKey = service.getPublicKey();
            if (publicKey == null) {
                logger.debug("No public key is defined for service [{}]. No encoding will take place.", service);
                return false;
            }
            logger.debug("Using public key [{}]:[{}] to initialize the cipher",
                    publicKey.getAlgorithm(), publicKey.getFormat());

            this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            logger.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}]",
                    publicKey.getAlgorithm());
            return true;
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final Service service) {
        logger.debug("Starting to encode attributes for release to service [{}]", service);
        final Map<String, Object> newEncodedAttributes = new HashMap<>(attributes);

        initialize(newEncodedAttributes);

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            encodeAttributesInternal(newEncodedAttributes, registeredService);
        }
        logger.debug("[{}] Encoded attributes are available for release to [{}]",
                newEncodedAttributes.size(), service);
        return newEncodedAttributes;
    }

    /**
     * Initialize the encoding process. Removes the
     * {@link UsernamePasswordCredential#AUTHENTICATION_ATTRIBUTE_PASSWORD}
     * from the authentication attributes originally and caches it, so it
     * can later on be encrypted if needed.
     * @param attributes the new encoded attributes
     */
    protected void initialize(final Map<String, Object> attributes) {

        broken...
        this.cachedCredential = (String) attributes.remove(
                UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);
        if (!StringUtils.isNotBlank(this.cachedCredential)) {
            logger.debug("Removed credential as an authentication attribute and cached it locally.");
        }
    }
}
