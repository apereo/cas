package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SamlAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlAttributeEncoder implements ProtocolAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlAttributeEncoder.class);

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService service) {
        final Map<String, Object> finalAttributes = new HashMap<>(attributes.size());

        attributes.forEach((k, v) -> {
            final String attributeName = EncodingUtils.hexDecode(k);
            if (StringUtils.isNotBlank(attributeName)) {
                LOGGER.debug("Decoded SAML attribute [{}] to [{}] with value(s) [{}]", k, attributeName, v);
                finalAttributes.put(attributeName, v);
            } else {
                LOGGER.debug("Unable to decode SAML attribute [{}]; accepting it verbatim", k);
                finalAttributes.put(k, v);
            }
        });
        return finalAttributes;
    }
}

