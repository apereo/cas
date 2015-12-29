package org.jasig.cas.support.saml;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is {@link SamlIdPUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class SamlIdPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlIdPUtils.class);

    private SamlIdPUtils() {
    }

    /**
     * Prepare peer entity saml endpoint.
     *
     * @param outboundContext the outbound context
     * @param adaptor         the adaptor
     * @throws SamlException the saml exception
     */
    public static void preparePeerEntitySamlEndpointContext(final MessageContext outboundContext, final SamlMetadataAdaptor adaptor)
            throws SamlException {
        final List<AssertionConsumerService> assertionConsumerServices = adaptor.getAssertionConsumerServices();
        if (assertionConsumerServices.isEmpty()) {
            throw new SamlException(SamlException.CODE, "No assertion consumer service could be found for entity " + adaptor.getEntityId());
        }

        final SAMLPeerEntityContext peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
        if (peerEntityContext == null) {
            throw new SamlException(SamlException.CODE, "SAMLPeerEntityContext could not be defined for entity " + adaptor.getEntityId());
        }

        final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
        if (endpointContext == null) {
            throw new SamlException(SamlException.CODE, "SAMLEndpointContext could not be defined for entity " + adaptor.getEntityId());
        }
        final Endpoint endpoint = assertionConsumerServices.get(0);
        if (StringUtils.isBlank(endpoint.getBinding()) || StringUtils.isBlank(endpoint.getLocation())) {
            throw new SamlException(SamlException.CODE, "Assertion consumer service does not define a binding or location for "
                    + adaptor.getEntityId());
        }
        LOGGER.debug("Configured peer entity endpoint to be [{}] with binding [{}]", endpoint.getLocation(), endpoint.getBinding());
        endpointContext.setEndpoint(endpoint);
    }
}


