package org.apereo.cas.support.claims;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.EncodingUtils;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * This is {@link WsFederationClaimsEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class WsFederationClaimsEncoder implements ProtocolAttributeEncoder {

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService service) {
        final Map<String, Object> finalAttributes = Maps.newHashMapWithExpectedSize(attributes.size());

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

    /**
     * Encode claim string.
     *
     * @param claim the claim
     * @return the string
     */
    public String encodeClaim(final String claim) {
        final String attributeName = EncodingUtils.hexDecode(claim);
        if (StringUtils.isNotBlank(attributeName)) {
            LOGGER.debug("Decoded SAML attribute [{}] to [{}] with value(s) [{}]", claim, attributeName, claim);
            return attributeName;
        }
        LOGGER.debug("Unable to decode SAML attribute [{}]; accepting it verbatim", claim);
        return claim;
    }
}

