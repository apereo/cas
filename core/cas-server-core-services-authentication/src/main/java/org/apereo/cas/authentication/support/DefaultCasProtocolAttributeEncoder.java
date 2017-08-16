package org.apereo.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
public class DefaultCasProtocolAttributeEncoder extends AbstractProtocolAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasProtocolAttributeEncoder.class);

    private final CipherExecutor<String, String> cacheCredentialCipherExecutor;

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager               the services manager
     * @param cacheCredentialCipherExecutor the cache credential cipher executor
     */
    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager,
                                              final CipherExecutor cacheCredentialCipherExecutor) {
        this(servicesManager, new DefaultRegisteredServiceCipherExecutor(), cacheCredentialCipherExecutor);
    }

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager               the services manager
     * @param cipherExecutor                the cipher executor
     * @param cacheCredentialCipherExecutor the cache credential cipher executor
     */
    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager,
                                              final RegisteredServiceCipherExecutor cipherExecutor,
                                              final CipherExecutor cacheCredentialCipherExecutor) {
        super(servicesManager, cipherExecutor);
        this.cacheCredentialCipherExecutor = cacheCredentialCipherExecutor;
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes,
                                                      final Map<String, String> cachedAttributesToEncode,
                                                      final RegisteredServiceCipherExecutor cipher,
                                                      final RegisteredService registeredService) {

        if (cachedAttributesToEncode.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL)) {
            final String value = cachedAttributesToEncode.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
            final String decodedValue = this.cacheCredentialCipherExecutor.decode(value);
            cachedAttributesToEncode.remove(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
            if (StringUtils.isNotBlank(decodedValue)) {
                cachedAttributesToEncode.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, decodedValue);
            }
        }

        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                cipher, registeredService);
    }

    /**
     * Encode and encrypt pgt.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> attributes,
                                                       final Map<String, String> cachedAttributesToEncode,
                                                       final RegisteredServiceCipherExecutor cipher,
                                                       final RegisteredService registeredService) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, cipher, registeredService);
    }

    /**
     * Encrypt, encode and put the attribute into attributes map.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cachedAttributeName      the cached attribute name
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName,
                                                           final RegisteredServiceCipherExecutor cipher,
                                                           final RegisteredService registeredService) {
        final String cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            LOGGER.debug("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            final String encodedValue = cipher.encode(cachedAttribute, registeredService);
            if (StringUtils.isNotBlank(encodedValue)) {
                attributes.put(cachedAttributeName, encodedValue);
                LOGGER.debug("Encrypted and encoded [{}] as an attribute to [{}].", cachedAttributeName, encodedValue);
            } else {
                LOGGER.warn("Attribute [{}] cannot be encoded and is removed from the collection of attributes", cachedAttributeName);
            }
        } else {
            LOGGER.debug("[{}] is not available as a cached model attribute to encrypt...", cachedAttributeName);
        }
    }

    @Override
    protected void encodeAttributesInternal(final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final RegisteredServiceCipherExecutor cipher,
                                            final RegisteredService registeredService) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher, registeredService);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode, cipher, registeredService);
        sanitizeAndTransformAttributeNames(attributes, registeredService);
    }

    private static void sanitizeAndTransformAttributeNames(final Map<String, Object> attributes,
                                                           final RegisteredService registeredService) {
        LOGGER.debug("Sanitizing attribute names in preparation of the final validation response");

        final Set<Pair<String, Object>> attrs = attributes.keySet().stream()
                .filter(getSanitizingAttributeNamePredicate())
                .map(s -> Pair.of(EncodingUtils.hexEncode(s.getBytes(StandardCharsets.UTF_8)), attributes.get(s)))
                .collect(Collectors.toSet());
        if (!attrs.isEmpty()) {
            LOGGER.warn("Found [{}] attribute(s) that need to be sanitized/encoded.", attrs);
            attributes.keySet().removeIf(getSanitizingAttributeNamePredicate());
            attrs.forEach(p -> {
                LOGGER.debug("Sanitized attribute name to be [{}]", p.getKey());
                attributes.put(p.getKey(), p.getValue());
            });
        }
    }

    private static Predicate<String> getSanitizingAttributeNamePredicate() {
        return s -> s.contains(":") || s.contains("@");
    }
}
