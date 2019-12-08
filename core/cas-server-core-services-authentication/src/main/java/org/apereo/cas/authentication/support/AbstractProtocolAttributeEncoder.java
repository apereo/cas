package org.apereo.cas.authentication.support;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to define common attribute encoding operations.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public abstract class AbstractProtocolAttributeEncoder implements ProtocolAttributeEncoder {

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    private final RegisteredServiceCipherExecutor cipherExecutor;

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService registeredService) {
        LOGGER.trace("Starting to encode attributes for release to service [{}]", registeredService);
        val newEncodedAttributes = new HashMap<String, Object>(attributes);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            val cachedAttributesToEncode = initialize(newEncodedAttributes);
            encodeAttributesInternal(newEncodedAttributes, cachedAttributesToEncode, this.cipherExecutor, registeredService);
            LOGGER.debug("[{}] encoded attributes are available for release to [{}]: [{}]",
                newEncodedAttributes.size(), registeredService.getName(), newEncodedAttributes.keySet());
        } else {
            LOGGER.debug("Service is not found/enabled in the service registry so no encoding has taken place.");
        }
        return newEncodedAttributes;
    }

    /**
     * Initialize the cipher with the public key
     * and then start to encrypt select attributes.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher                   the cipher object initialized per service public key
     * @param registeredService        the registered service
     */
    protected abstract void encodeAttributesInternal(Map<String, Object> attributes, Map<String, String> cachedAttributesToEncode,
                                                     RegisteredServiceCipherExecutor cipher, RegisteredService registeredService);

    /**
     * Initialize the encoding process. Removes the
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL}
     * and
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET}
     * from the authentication attributes originally and into a cache object, so it
     * can later on be encrypted if needed.
     *
     * @param attributes the new encoded attributes
     * @return a map of attributes that are to be encoded and encrypted
     */
    protected Map<String, String> initialize(final Map<String, Object> attributes) {
        val cachedAttributesToEncode = new HashMap<String, String>();
        removeAttributeAndCacheForEncoding(attributes, cachedAttributesToEncode, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        removeAttributeAndCacheForEncoding(attributes, cachedAttributesToEncode, CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        removeAttributeAndCacheForEncoding(attributes, cachedAttributesToEncode, CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU);
        return cachedAttributesToEncode;
    }

    private static void removeAttributeAndCacheForEncoding(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String attributeName) {
        val messageFormat = "Removed [{}] as an authentication attribute and cached it locally.";
        val collection = (Collection<?>) attributes.remove(attributeName);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(attributeName, collection.iterator().next().toString());
            LOGGER.debug(messageFormat, attributeName);
        }
    }
}
