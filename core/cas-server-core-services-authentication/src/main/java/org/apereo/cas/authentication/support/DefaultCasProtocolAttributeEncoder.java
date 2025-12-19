package org.apereo.cas.authentication.support;

import module java.base;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.ValidationResponseType;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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

    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager,
                                              final RegisteredServiceCipherExecutor cipherExecutor,
                                              final CipherExecutor<String, String> cacheCredentialCipherExecutor) {
        super(servicesManager, cipherExecutor);
        this.cacheCredentialCipherExecutor = cacheCredentialCipherExecutor;
    }

    private static void sanitizeAndTransformAttributeNames(final Map<String, Object> attributes,
                                                           final Service webApplicationService) {
        if (webApplicationService instanceof final WebApplicationService was && was.getFormat() == ValidationResponseType.JSON) {
            LOGGER.trace("Skipping attribute name sanitization for [{}]", webApplicationService);
            return;
        }

        LOGGER.trace("Sanitizing attribute names in preparation of the final validation response");
        val encodedAttributes = attributes.keySet()
            .stream()
            .filter(DefaultCasProtocolAttributeEncoder::getSanitizingAttributeNamePredicate)
            .map(attribute -> {
                val values = attributes.get(attribute);
                LOGGER.trace("Encoding attribute [{}] with value(s) [{}]", attribute, values);
                return Pair.of(ProtocolAttributeEncoder.encodeAttribute(attribute), values);
            })
            .collect(Collectors.toSet());

        if (!encodedAttributes.isEmpty()) {
            LOGGER.info("Found [{}] attribute(s) that need to be sanitized/encoded.", encodedAttributes.size());
            attributes.keySet().removeIf(DefaultCasProtocolAttributeEncoder::getSanitizingAttributeNamePredicate);
            encodedAttributes.forEach(attribute -> {
                val key = attribute.getKey();
                LOGGER.trace("Sanitized attribute name to be [{}]", key);
                attributes.put(key, transformAttributeValueIfNecessary(attribute.getValue()));
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

    protected void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> model,
                                                       final Map<String, Object> resultingAttributes,
                                                       final Map<String, String> cachedAttributesToEncode,
                                                       final RegisteredServiceCipherExecutor cipher,
                                                       final RegisteredService registeredService) {

        if (model.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET)) {
            val proxyGrantingTicket = (Ticket) model.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
            if (proxyGrantingTicket != null && proxyGrantingTicket.isStateless()) {
                putIntoAttributesMap(resultingAttributes, cachedAttributesToEncode,
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
            } else {
                encryptAndEncodeAndPutIntoAttributesMap(resultingAttributes, cachedAttributesToEncode,
                    CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, cipher, registeredService);
            }
        }

        encryptAndEncodeAndPutIntoAttributesMap(resultingAttributes, cachedAttributesToEncode,
            CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, cipher, registeredService);
    }

    protected void putIntoAttributesMap(final Map<String, Object> resultingAttributes,
                                        final Map<String, String> cachedAttributesToEncode,
                                        final String cachedAttributeName) {
        val cachedAttributeValue = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttributeValue)) {
            resultingAttributes.put(cachedAttributeName, cachedAttributeValue);
        }
    }

    protected void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> resultingAttributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName,
                                                           final RegisteredServiceCipherExecutor cipher,
                                                           final RegisteredService registeredService) {
        val cachedAttributeValue = cachedAttributesToEncode.remove(cachedAttributeName);
        val encodedValue = StringUtils.isNotBlank(cachedAttributeValue)
            ? cipher.encode(cachedAttributeValue, Optional.of(registeredService))
            : cachedAttributeValue;
        if (StringUtils.isNotBlank(encodedValue)) {
            resultingAttributes.put(cachedAttributeName, encodedValue);
        }
    }

    @Override
    protected void encodeAttributesInternal(final Map<String, Object> model,
                                            final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final RegisteredServiceCipherExecutor cipher,
                                            final RegisteredService registeredService,
                                            final Service webApplicationService) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher, registeredService);
        encodeAndEncryptProxyGrantingTicket(model, attributes, cachedAttributesToEncode, cipher, registeredService);
        sanitizeAndTransformAttributeNames(attributes, webApplicationService);
        sanitizeAndTransformAttributeValues(attributes);
    }
}
