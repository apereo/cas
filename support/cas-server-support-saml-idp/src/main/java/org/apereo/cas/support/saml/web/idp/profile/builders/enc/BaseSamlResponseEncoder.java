package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseSamlResponseEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseSamlResponseEncoder {
    /**
     * The Velocity engine factory.
     */
    protected final VelocityEngineFactory velocityEngineFactory;
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
    
    public BaseSamlResponseEncoder(final VelocityEngineFactory velocityEngineFactory,
                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                   final HttpServletResponse httpResponse,
                                   final HttpServletRequest httpRequest) {
        this.velocityEngineFactory = velocityEngineFactory;
        this.adaptor = adaptor;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    /**
     * Encode.
     *
     * @param samlResponse the saml response
     * @param relayState   the relay state
     * @param binding      the binding
     * @return the response
     * @throws SamlException the saml exception
     */
    public final Response encode(final Response samlResponse, final String relayState, final String binding) throws SamlException {

        try {
            if (httpResponse != null) {
                final BaseSAML2MessageEncoder encoder = getMessageEncoderInstance();
                encoder.setHttpServletResponse(httpResponse);
                final MessageContext outboundMessageContext = new MessageContext<>();
                outboundMessageContext.setMessage(samlResponse);
                SAMLBindingSupport.setRelayState(outboundMessageContext, relayState);
                SamlIdPUtils.preparePeerEntitySamlEndpointContext(outboundMessageContext, adaptor, binding);
                encoder.setMessageContext(outboundMessageContext);
                finalizeEncode(encoder, samlResponse);
            }
            return samlResponse;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Finalize encode response.
     *
     * @param encoder      the encoder
     * @param samlResponse the saml response
     * @return the response
     * @throws Exception the saml exception
     */
    protected void finalizeEncode(final BaseSAML2MessageEncoder encoder,
                                  final SAMLObject samlResponse) throws Exception {
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
