package org.apereo.cas.authentication.support;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to define common attribute encoding operations.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractCasAttributeEncoder implements CasAttributeEncoder {
    
    /** The Services manager. */
    protected ServicesManager servicesManager;

    /** The Logger. */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private RegisteredServiceCipherExecutor cipherExecutor;

    /**
     * Instantiates a new abstract cas attribute encoder.
     */
    protected AbstractCasAttributeEncoder() {}

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
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes,
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
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL}
     * and
     * {@link CasViewConstants#MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET}
     * from the authentication attributes originally and into a cache object, so it
     * can later on be encrypted if needed.
     * @param attributes the new encoded attributes
     * @return a map of attributes that are to be encoded and encrypted
     */
    protected Map<String, String> initialize(final Map<String, Object> attributes) {
        final Map<String, String> cachedAttributesToEncode = new HashMap<>(attributes.size());

        final String messageFormat = "Removed [{}] as an authentication attribute and cached it locally.";
        
        Collection<?> collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                    collection.iterator().next().toString());
            logger.debug(messageFormat,
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        }

        collection = (Collection<?>) attributes.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        if (collection != null && collection.size() == 1) {
            cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET,
                    collection.iterator().next().toString());
            logger.debug(messageFormat,
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        }
        return cachedAttributesToEncode;
    }

    public void setCipherExecutor(final RegisteredServiceCipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }
}
