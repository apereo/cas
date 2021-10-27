package org.jasig.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceCipherExecutor;
import org.jasig.cas.services.ServicesManager;

import org.jasig.cas.CasViewConstants;
import org.springframework.stereotype.Component;

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
@Component("casAttributeEncoder")
public class DefaultCasAttributeEncoder extends AbstractCasAttributeEncoder {

    /**
     * Instantiates a new Default cas attribute encoder.
     */
    protected DefaultCasAttributeEncoder() {
        super();
    }

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager the services manager
     */
    public DefaultCasAttributeEncoder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager the services manager
     * @param cipherExecutor the cipher executor
     */
    public DefaultCasAttributeEncoder(final ServicesManager servicesManager,
                                      final RegisteredServiceCipherExecutor cipherExecutor) {
        super(servicesManager, cipherExecutor);
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
     * @param registeredService the registered service
     */
    protected final void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes,
                                                      final Map<String, String> cachedAttributesToEncode,
                                                      final RegisteredServiceCipherExecutor cipher,
                                                      final RegisteredService registeredService) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                cipher, registeredService);
    }

    /**
     * Encode and encrypt pgt.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher the cipher
     * @param registeredService the registered service
     */
    protected final void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> attributes,
                                                       final Map<String, String> cachedAttributesToEncode,
                                                       final RegisteredServiceCipherExecutor cipher,
                                                       final RegisteredService registeredService) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, cipher, registeredService);
    }

    /**
     * Encrypt, encode and put the attribute into attributes map.
     *
     * @param attributes the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cachedAttributeName the cached attribute name
     * @param cipher the cipher
     * @param registeredService the registered service
     */
    protected final void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName,
                                                           final RegisteredServiceCipherExecutor cipher,
                                                           final RegisteredService registeredService) {
        final String cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            logger.debug("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            final String encodedValue = cipher.encode(cachedAttribute, registeredService);
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
                                            final RegisteredServiceCipherExecutor cipher,
                                            final RegisteredService registeredService) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher, registeredService);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode, cipher, registeredService);
    }
}
