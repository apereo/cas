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
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.cipher.CipherExecutor;
import org.jasig.cas.web.view.CasViewConstants;
import org.jose4j.lang.StringUtil;

import javax.crypto.Cipher;
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
public class DefaultCasAttributeEncoder extends AbstractCasAttributeEncoder {

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager the services manager
     */
    public DefaultCasAttributeEncoder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher the cipher
     */
    protected final void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes,
                                                      final Map<String, String> cachedAttributesToEncode,
                                                      final CipherExecutor cipher) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                cipher);
    }

    /**
     * Encode and encrypt pgt.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher the cipher
     */
    protected final void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> attributes,
                                                       final Map<String, String> cachedAttributesToEncode,
                                                       final CipherExecutor cipher) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, cipher);
    }

    /**
     * Encrypt, encode and put the attribute into attributes map.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cachedAttributeName the cached attribute name
     * @param cipher the cipher
     */
    protected final void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName,
                                                           final CipherExecutor cipher) {
        final String cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            logger.debug("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            final String encodedValue = cipher.encode(cachedAttribute);
            if (StringUtils.isNotBlank(encodedValue)) {
                attributes.put(cachedAttributeName, encodedValue);
                logger.debug("Encrypted and encoded [{}] as an attribute to [{}].",
                        cachedAttributeName, encodedValue);
            }
        } else {
            logger.debug("[{}] is not available as a cached model attribute to encrypt...", cachedAttributeName);
        }
    }

    @Override
    protected void encodeAttributesInternal(final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final CipherExecutor cipher) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode, cipher);
    }
}
