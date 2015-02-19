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

package org.jasig.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.web.view.CasViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;
import java.util.Collection;
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

    /**
     * Instantiates a new attribute encoder.
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
        logger.debug("Created cipher instance to encrypt via [{}]", cipherAlgorithm);
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     */
    protected final void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes,
                                                      final Map<String, String> cachedAttributesToEncode) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
    }

    /**
     * Encode and encrypt pgt using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     */
    protected final void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> attributes,
                                                       final Map<String, String> cachedAttributesToEncode) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
    }

    /**
     * Encrypt, encode and put the attribute into attributes map.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cachedAttributeName the cached attribute name
     */
    protected final void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName) {
        final String cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            logger.debug("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            final String encodedValue = CompressionUtils.encryptAndEncodeBase64(cachedAttribute, this.cipher);
            attributes.put(cachedAttributeName, encodedValue);
            logger.debug("Encrypted and encoded [{}] as an attribute to [{}].", cachedAttributeName, encodedValue);
        } else {
            logger.debug("[{}] is not available as a cached model attribute to encrypt...", cachedAttributeName);
        }
    }

    /**
     * Initialize the cipher with the public key
     * and then start to encrypt select attributes.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param service the service
     */
    protected void encodeAttributesInternal(final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final RegisteredService service) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode);
    }

    /**
     * Initialize cipher based on service public key.
     *
     * @param service the service
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    protected final boolean initializeCipherBasedOnServicePublicKey(final RegisteredService service) {
        try {
            final PublicKey publicKey = service.getPublicKey().createInstance();
            if (publicKey == null) {
                logger.debug("No public key is defined for service [{}]. No encoding will take place.", service);
                return false;
            }
            logger.debug("Using public key [{}] to initialize the cipher", service.getPublicKey());

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
    public final Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final Service service) {
        logger.debug("Starting to encode attributes for release to service [{}]", service);
        final Map<String, Object> newEncodedAttributes = new HashMap<>(attributes);

        final Map<String, String> cachedAttributesToEncode = initialize(newEncodedAttributes);

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            if (initializeCipherBasedOnServicePublicKey(registeredService)) {
                encodeAttributesInternal(newEncodedAttributes, cachedAttributesToEncode, registeredService);
            } else {
                logger.warn("Cipher could not be initialized for service [{}]. No encryption was applied to attributes",
                        service);
            }
        } else {
            logger.warn("Service [{}] is not found and/or enabled in the service registry.", service);
        }
        logger.debug("[{}] Encoded attributes are available for release to [{}]",
                newEncodedAttributes.size(), service);
        return newEncodedAttributes;
    }

    /**
     * Initialize the encoding process. Removes the
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL}
     * and
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET}
     * from the authentication attributes originally and into a cache object, so it
     * can later on be encrypted if needed.
     * @param attributes the new encoded attributes
     * @return a map of attributes that are to be encoded and encrypted
     */
    protected final Map<String, String> initialize(final Map<String, Object> attributes) {
        final Map<String, String> cachedAttributesToEncode = new HashMap<>(attributes.size());

        Collection<?> collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                    collection.iterator().next().toString());
            logger.debug("Removed credential as an authentication attribute and cached it locally.");
        }

        collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        if (collection != null && collection.size() == 1) {
           cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
                   collection.iterator().next().toString());
            logger.debug("Removed PGT as an authentication attribute and cached it locally.");
        }
        return cachedAttributesToEncode;
    }
}
