package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.EncodingUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
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
public class SamlAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlAttributeEncoder.class);

    /**
     * Encode and transform attributes.
     *
     * @param authnRequest the authn request
     * @param attributes   the attributes
     * @param service      the service
     * @param adaptor      the service provider facade
     * @return the map
     */
    public Map<String, Object> encode(final AuthnRequest authnRequest, final Map<String, Object> attributes,
                                      final SamlRegisteredService service,
                                      final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        return decodeAttributes(attributes);
    }

    /**
     * Decode attributes map.
     *
     * @param receivedAttributes the received attributes
     * @return the map
     */
    protected Map<String, Object> decodeAttributes(final Map<String, Object> receivedAttributes) {
        final Map<String, Object> finalAttributes = new HashMap<>(receivedAttributes.size());

        receivedAttributes.forEach((k, v) -> {
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
