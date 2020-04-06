package org.apereo.cas.authentication.support;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Optional;
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
@Slf4j
public class DefaultCasProtocolAttributeEncoder extends AbstractProtocolAttributeEncoder {
    private final CipherExecutor<String, String> cacheCredentialCipherExecutor;

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager               the services manager
     * @param cacheCredentialCipherExecutor the cache credential cipher executor
     */
    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager,
                                              final CipherExecutor<String, String> cacheCredentialCipherExecutor) {
        this(servicesManager, new RegisteredServicePublicKeyCipherExecutor(), cacheCredentialCipherExecutor);
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
                                              final CipherExecutor<String, String> cacheCredentialCipherExecutor) {
        super(servicesManager, cipherExecutor);
        this.cacheCredentialCipherExecutor = cacheCredentialCipherExecutor;
    }

    private static void sanitizeAndTransformAttributeNames(final Map<String, Object> attributes) {
        LOGGER.trace("Sanitizing attribute names in preparation of the final validation response");

        val attrs = attributes.keySet().stream()
            .filter(DefaultCasProtocolAttributeEncoder::getSanitizingAttributeNamePredicate)
            .map(s -> Pair.of(ProtocolAttributeEncoder.encodeAttribute(s), attributes.get(s)))
            .collect(Collectors.toSet());

        if (!attrs.isEmpty()) {
            LOGGER.info("Found [{}] attribute(s) that need to be sanitized/encoded.", attrs);
            attributes.keySet().removeIf(DefaultCasProtocolAttributeEncoder::getSanitizingAttributeNamePredicate);
            attrs.forEach(p -> {
                val key = p.getKey();
                LOGGER.trace("Sanitized attribute name to be [{}]", key);
                attributes.put(key, transformAttributeValueIfNecessary(p.getValue()));
            });
        }
    }

    private static boolean getSanitizingAttributeNamePredicate(final String s) {
        return s.contains(":") || s.contains("@");
    }

    private static void sanitizeAndTransformAttributeValues(final Map<String, Object> attributes) {
        LOGGER.trace("Sanitizing attribute values in preparation of the final validation response");
        attributes.forEach((key, value) -> {
            val values = CollectionUtils.toCollection(value);
            values.stream()
                .filter(v -> getBinaryAttributeValuePredicate().test(v))
                .forEach(v -> attributes.put(key, transformAttributeValueIfNecessary(v)));
        });
    }

    private static Object transformAttributeValueIfNecessary(final Object attributeValue) {
        if (getBinaryAttributeValuePredicate().test(attributeValue)) {
            return EncodingUtils.encodeBase64((byte[]) attributeValue);
        }
        return attributeValue;
    }


    private static Predicate<Object> getBinaryAttributeValuePredicate() {
        return Predicates.instanceOf(byte[].class);
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
            val value = cachedAttributesToEncode.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
            val decodedValue = this.cacheCredentialCipherExecutor.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
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
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, cipher, registeredService);
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
        val cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            LOGGER.trace("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            val encodedValue = cipher.encode(cachedAttribute, Optional.of(registeredService));
            if (StringUtils.isNotBlank(encodedValue)) {
                attributes.put(cachedAttributeName, encodedValue);
                LOGGER.trace("Encrypted and encoded [{}] as an attribute to [{}].", cachedAttributeName, encodedValue);
            } else {
                LOGGER.warn("Attribute [{}] cannot be encoded and is removed from the collection of attributes", cachedAttributeName);
            }
        } else {
            LOGGER.trace("[{}] is not available as a cached model attribute to encrypt...", cachedAttributeName);
        }
    }

    @Override
    protected void encodeAttributesInternal(final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final RegisteredServiceCipherExecutor cipher,
                                            final RegisteredService registeredService) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher, registeredService);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode, cipher, registeredService);
        sanitizeAndTransformAttributeNames(attributes);
        sanitizeAndTransformAttributeValues(attributes);
    }
}
