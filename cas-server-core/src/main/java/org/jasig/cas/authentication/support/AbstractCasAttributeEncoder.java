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

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.services.RegisteredServiceCipherExecutor;
import org.jasig.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.jasig.cas.web.view.CasViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to define common attribute encoding operations.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractCasAttributeEncoder implements CasAttributeEncoder {

    /** The Services manager. */
    @NotNull
    protected final ServicesManager servicesManager;

    /** The Logger. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RegisteredServiceCipherExecutor cipherExecutor;

    /**
     * Instantiates a new attribute encoder with the default
     * cipher as {@link DefaultRegisteredServiceCipherExecutor}.
     * @param servicesManager the services manager
     */
    public AbstractCasAttributeEncoder(final ServicesManager servicesManager) {
        this(servicesManager, new DefaultRegisteredServiceCipherExecutor());
    }

    /**
     * Instantiates a new Abstract cas attribute encoder.
     *
     * @param servicesManager the services manager
     * @param cipherExecutor the cipher executor
     */
    public AbstractCasAttributeEncoder(final ServicesManager servicesManager,
                                       final RegisteredServiceCipherExecutor cipherExecutor) {
        this.servicesManager = servicesManager;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public final Map<String, Object> encodeAttributes(final Map<String, Object> attributes,
                                                      final Service service) {
        logger.debug("Starting to encode attributes for release to service [{}]", service);
        final Map<String, Object> newEncodedAttributes = new HashMap<>(attributes);
        final Map<String, String> cachedAttributesToEncode = initialize(newEncodedAttributes);

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            encodeAttributesInternal(newEncodedAttributes, cachedAttributesToEncode,
                    this.cipherExecutor, registeredService);
            logger.debug("[{}] Encoded attributes are available for release to [{}]",
                    newEncodedAttributes.size(), service);
        } else {
            logger.debug("Service [{}] is not found and/or enabled in the service registry. "
                    + "No encoding has taken place.", service);
        }

        return newEncodedAttributes;
    }

    /**
     * Initialize the cipher with the public key
     * and then start to encrypt select attributes.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher the cipher object initialized per service public key
     * @param registeredService the registered service
     */
    protected abstract void encodeAttributesInternal(Map<String, Object> attributes,
                                            Map<String, String> cachedAttributesToEncode,
                                            RegisteredServiceCipherExecutor cipher,
                                            RegisteredService registeredService);


    /**
     * Initialize the encoding process. Removes the
     * {@link org.jasig.cas.web.view.CasViewConstants#MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL}
     * and
     * {@link org.jasig.cas.web.view.CasViewConstants#MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET}
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
            logger.debug("Removed [{}] as an authentication attribute and cached it locally.",
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        }

        collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
                    collection.iterator().next().toString());
            logger.debug("Removed [{}] as an authentication attribute and cached it locally.",
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        }
        return cachedAttributesToEncode;
    }
}
