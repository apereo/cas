package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * An encoder that defines how a CAS attribute
 * is to be encoded and signed in the CAS
 * validation response. The collection of
 * attributes should not be mangled with and
 * filtered. All attributes will be released.
 * It is up to the implementations
 * to decide which attribute merits encrypting.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface ProtocolAttributeEncoder {
    Logger LOGGER = LoggerFactory.getLogger(ProtocolAttributeEncoder.class);

    /**
     * The constant ENCODED_ATTRIBUTE_PREFIX.
     */
    String ENCODED_ATTRIBUTE_PREFIX = "_";

    /**
     * Encodes attributes that are ready to be released.
     * Typically, this method tries to ensure that the
     * PGT and the credential password are correctly encrypted
     * before they are released. Attributes should not be filtered
     * and removed and it is assumed that all will be returned
     * back to the service.
     *
     * @param attributes            The attribute collection that is ready to be released
     * @param registeredService     the requesting service for which attributes are to be encoded
     * @param webApplicationService the web application service
     * @return collection of attributes after encryption ready for release.
     * @since 4.1
     */
    default Map<String, Object> encodeAttributes(final Map<String, Object> attributes,
        final RegisteredService registeredService, final WebApplicationService webApplicationService) {
        val finalAttributes = Maps.<String, Object>newHashMapWithExpectedSize(attributes.size());
        attributes.forEach((k, v) -> {
            val attributeName = decodeAttribute(k);
            LOGGER.debug("Decoded attribute [{}] to [{}] with value(s) [{}]", k, attributeName, v);
            finalAttributes.put(attributeName, v);
        });
        return finalAttributes;
    }

    /**
     * Is attribute name encoded boolean.
     *
     * @param name the name
     * @return true/false
     */
    static boolean isAttributeNameEncoded(final String name) {
        return name.startsWith(ENCODED_ATTRIBUTE_PREFIX);
    }

    /**
     * Encode attribute string.
     *
     * @param s the s
     * @return the string
     */
    static String encodeAttribute(final String s) {
        return ENCODED_ATTRIBUTE_PREFIX + new String(Hex.encodeHex(s.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Decode attribute string.
     *
     * @param s the s
     * @return the string
     */
    @SneakyThrows
    static String decodeAttribute(final String s) {
        if (isAttributeNameEncoded(s)) {
            return new String(Hex.decodeHex(s.substring(1)), StandardCharsets.UTF_8);
        }
        return s;
    }

    /**
     * Decode attributes map.
     *
     * @param attributes            the attributes
     * @param registeredService     the registered service
     * @param webApplicationService the web application service
     * @return the map
     */
    static Map<String, Object> decodeAttributes(final Map<String, Object> attributes,
                                                final RegisteredService registeredService,
                                                final WebApplicationService webApplicationService) {
        val finalAttributes = Maps.<String, Object>newHashMapWithExpectedSize(attributes.size());
        attributes.forEach((k, v) -> {
            val attributeName = ProtocolAttributeEncoder.decodeAttribute(k);
            LOGGER.debug("Decoded SAML attribute [{}] to [{}] with value(s) [{}]", k, attributeName, v);
            finalAttributes.put(attributeName, v);
        });
        return finalAttributes;
    }
}
