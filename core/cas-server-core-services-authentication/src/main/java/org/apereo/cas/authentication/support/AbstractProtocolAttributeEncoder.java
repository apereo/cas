package org.apereo.cas.authentication.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceCipherExecutor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/**
 * Abstract class to define common attribute encoding operations.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Setter
@AllArgsConstructor
public abstract class AbstractProtocolAttributeEncoder implements ProtocolAttributeEncoder {

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    private RegisteredServiceCipherExecutor cipherExecutor;

    /**
     * Instantiates a new attribute encoder with the default
     * cipher as {@link DefaultRegisteredServiceCipherExecutor}.
     *
     * @param servicesManager the services manager
     */
    public AbstractProtocolAttributeEncoder(final ServicesManager servicesManager) {
        this(servicesManager, new DefaultRegisteredServiceCipherExecutor());
    }

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService registeredService) {
        LOGGER.debug("Starting to encode attributes for release to service [{}]", registeredService);
        final Map<String, Object> newEncodedAttributes = new HashMap<>(attributes);
        final Map<String, String> cachedAttributesToEncode = initialize(newEncodedAttributes);
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            encodeAttributesInternal(newEncodedAttributes, cachedAttributesToEncode, this.cipherExecutor, registeredService);
            LOGGER.debug("[{}] encoded attributes are available for release to [{}]: [{}]", newEncodedAttributes.size(), registeredService, newEncodedAttributes.keySet());
        } else {
            LOGGER.debug("Service [{}] is not found/enabled in the service registry so no encoding has taken place.", registeredService);
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
        final Map<String, String> cachedAttributesToEncode = new HashMap<>(attributes.size());
        final String messageFormat = "Removed [{}] as an authentication attribute and cached it locally.";
        Collection<?> collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, collection.iterator().next().toString());
            LOGGER.debug(messageFormat, CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        }
        collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, collection.iterator().next().toString());
            LOGGER.debug(messageFormat, CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        }
        return cachedAttributesToEncode;
    }
}
