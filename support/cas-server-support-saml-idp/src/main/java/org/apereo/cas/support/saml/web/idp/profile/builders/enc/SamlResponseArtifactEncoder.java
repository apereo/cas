package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.common.messaging.context.SAMLArtifactContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.artifact.SAML2ArtifactType0004;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPArtifactEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlResponseArtifactEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlResponseArtifactEncoder extends BaseSamlResponseEncoder {
    private final TicketRegistry ticketRegistry;
    private final SamlArtifactTicketFactory samlArtifactTicketFactory;
    private final RequestAbstractType authnRequest;
    private final SAMLArtifactMap samlArtifactMap;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    public SamlResponseArtifactEncoder(final VelocityEngineFactory velocityEngineFactory,
                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       final HttpServletRequest httpRequest,
                                       final HttpServletResponse httpResponse, final RequestAbstractType authnRequest,
                                       final TicketRegistry ticketRegistry,
                                       final SamlArtifactTicketFactory samlArtifactTicketFactory,
                                       final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                       final SAMLArtifactMap samlArtifactMap) {
        super(velocityEngineFactory, adaptor, httpResponse, httpRequest);
        this.ticketRegistry = ticketRegistry;
        this.samlArtifactTicketFactory = samlArtifactTicketFactory;
        this.authnRequest = authnRequest;
        this.samlArtifactMap = samlArtifactMap;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    @Override
    protected String getBinding() {
        return SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
    }

    @Override
    protected BaseSAML2MessageEncoder getMessageEncoderInstance() throws Exception {
        final HTTPArtifactEncoder encoder = new HTTPArtifactEncoder();
        encoder.setVelocityEngine(this.velocityEngineFactory.createVelocityEngine());
        return encoder;
    }

    @Override
    protected void finalizeEncode(final BaseSAML2MessageEncoder e,
                                  final Response samlResponse,
                                  final String relayState) throws Exception {
        final HTTPArtifactEncoder encoder = (HTTPArtifactEncoder) e;
        encoder.setArtifactMap(this.samlArtifactMap);

        final MessageContext ctx = getEncoderMessageContext(samlResponse, relayState);
        prepareArtifactContext(samlResponse, ctx);
        encoder.setMessageContext(ctx);
        super.finalizeEncode(encoder, samlResponse, relayState);
    }


    private void prepareArtifactContext(final Response samlResponse, final MessageContext ctx) {
        final SAMLArtifactContext art = ctx.getSubcontext(SAMLArtifactContext.class, true);
        art.setArtifactType(SAML2ArtifactType0004.TYPE_CODE);
        art.setSourceEntityId(samlResponse.getIssuer().getValue());
        final AssertionConsumerService svc = adaptor.getAssertionConsumerServiceForArtifactBinding();
        art.setSourceArtifactResolutionServiceEndpointIndex(svc.getIndex());
        art.setSourceArtifactResolutionServiceEndpointURL(svc.getLocation());
    }
}
