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
public final class DefaultCasAttributeEncoder implements CasAttributeEncoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private final String cipherAlgorithm;

    /** The Services manager. */
    @NotNull
    private final ServicesManager servicesManager;

    /** The Cipher instance used to encrypt attributes. */
    private final Cipher cipher;

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
        this.cipherAlgorithm = cipherAlgorithm;
        this.servicesManager = servicesManager;

        this.cipher = Cipher.getInstance(this.cipherAlgorithm);
        logger.debug("Created cipher instance to encrypt credential via [{}]", this.cipherAlgorithm);
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes the attributes
     */
    private void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes) {
        try {
            final String password = (String) attributes.get(
                    UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);

            logger.debug("Retrieved the password as an authentication attribute...");
            final byte[] cipherData = this.cipher.doFinal(password.getBytes());
            final String encPassword = CompressionUtils.encodeBase64(cipherData);

            attributes.remove(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);
            attributes.put(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, encPassword);
            logger.debug("Encrypted and encoded password as an authentication attribute.");

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
    private void encodeAttributesInternal(final Map<String, Object> attributes, final RegisteredService service) {
        try {
            final PublicKey publicKey = service.getPublicKey();
            if (publicKey != null) {

                this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                logger.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}]",
                        publicKey.getAlgorithm());

                encodeAndEncryptCredentialPassword(attributes);

            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final Service service) {
        final Map<String, Object> newEncodedAttributes = new HashMap<>(attributes);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            encodeAttributesInternal(newEncodedAttributes, registeredService);
        }
        return newEncodedAttributes;
    }
}
