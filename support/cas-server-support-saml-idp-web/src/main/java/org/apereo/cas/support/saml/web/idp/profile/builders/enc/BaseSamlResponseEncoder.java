package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.VelocityEngine;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseSamlResponseEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public abstract class BaseSamlResponseEncoder {
    /**
     * The Velocity engine factory.
     */
    protected final VelocityEngine velocityEngineFactory;
    /**
     * The Adaptor.
     */
    protected final SamlRegisteredServiceServiceProviderMetadataFacade adaptor;
    /**
     * The Http response.
     */
    protected final HttpServletResponse httpResponse;

    /**
     * The Http request.
     */
    protected final HttpServletRequest httpRequest;

    /**
     * Encode.
     *
     * @param samlResponse the saml response
     * @param relayState   the relay state
     * @return the response
     * @throws SamlException the saml exception
     */
    @SneakyThrows
    public final Response encode(final Response samlResponse, final String relayState) throws SamlException {
        if (httpResponse != null) {
            final BaseSAML2MessageEncoder encoder = getMessageEncoderInstance();
            encoder.setHttpServletResponse(httpResponse);

            final MessageContext ctx = getEncoderMessageContext(samlResponse, relayState);
            encoder.setMessageContext(ctx);
            finalizeEncode(encoder, samlResponse, relayState);
        }
        return samlResponse;

    }

    /**
     * Build encoder message context.
     *
     * @param samlResponse the saml response
     * @param relayState   the relay state
     * @return the message context
     */
    protected MessageContext getEncoderMessageContext(final Response samlResponse, final String relayState) {
        final MessageContext ctx = new MessageContext<>();
        ctx.setMessage(samlResponse);
        SAMLBindingSupport.setRelayState(ctx, relayState);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(ctx, adaptor, getBinding());
        final SAMLSelfEntityContext self = ctx.getSubcontext(SAMLSelfEntityContext.class, true);
        self.setEntityId(samlResponse.getIssuer().getValue());
        return ctx;
    }

    /**
     * Finalize encode response.
     *
     * @param encoder      the encoder
     * @param samlResponse the saml response
     * @param relayState   the relay state
     * @throws Exception the saml exception
     */
    protected void finalizeEncode(final BaseSAML2MessageEncoder encoder,
                                  final Response samlResponse,
                                  final String relayState) throws Exception {
        encoder.initialize();
        encoder.encode();
    }

    /**
     * Gets binding.
     *
     * @return the binding
     */
    protected abstract String getBinding();

    /**
     * Gets message encoder instance.
     *
     * @return the message encoder instance
     * @throws Exception the exception
     */
    protected abstract BaseSAML2MessageEncoder getMessageEncoderInstance() throws Exception;
}
