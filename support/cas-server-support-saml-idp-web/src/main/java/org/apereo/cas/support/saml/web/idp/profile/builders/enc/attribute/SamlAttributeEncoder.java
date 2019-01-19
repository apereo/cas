package org.apereo.cas.support.saml.web.idp.profile.builders.enc.attribute;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;

/**
 * This is {@link SamlAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlAttributeEncoder implements ProtocolAttributeEncoder {

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService service) {
        val finalAttributes = Maps.<String, Object>newHashMapWithExpectedSize(attributes.size());
        attributes.forEach((k, v) -> {
            val attributeName = ProtocolAttributeEncoder.decodeAttribute(k);
            LOGGER.debug("Decoded SAML attribute [{}] to [{}] with value(s) [{}]", k, attributeName, v);
            finalAttributes.put(attributeName, v);
        });
        return finalAttributes;
    }
}

