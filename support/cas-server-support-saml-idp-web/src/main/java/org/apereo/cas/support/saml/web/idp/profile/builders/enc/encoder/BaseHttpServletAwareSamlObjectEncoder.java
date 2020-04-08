package org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseHttpServletAwareSamlObjectEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public abstract class BaseHttpServletAwareSamlObjectEncoder<T extends SAMLObject> {
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
     * @param request    the request
     * @param samlObject the saml response
     * @param relayState the relay state
     * @return the response
     * @throws SamlException the saml exception
     */
    @SneakyThrows
    public final T encode(final RequestAbstractType request, final T samlObject, final String relayState) throws SamlException {
        if (httpResponse != null) {
            val encoder = getMessageEncoderInstance();
            encoder.setHttpServletResponse(httpResponse);

            val ctx = getEncoderMessageContext(request, samlObject, relayState);
            encoder.setMessageContext(ctx);
            finalizeEncode(request, encoder, samlObject, relayState);
        }
        return samlObject;

    }


    /**
     * Build encoder message context.
     *
     * @param request    the authn request
     * @param samlObject the saml response
     * @param relayState the relay state
     * @return the message context
     */
    protected MessageContext getEncoderMessageContext(final RequestAbstractType request, final T samlObject, final String relayState) {
        val ctx = new MessageContext();
        ctx.setMessage(samlObject);
        SAMLBindingSupport.setRelayState(ctx, relayState);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(request, ctx, adaptor, getBinding());
        val self = ctx.getSubcontext(SAMLSelfEntityContext.class, true);
        self.setEntityId(SamlIdPUtils.getIssuerFromSamlObject(samlObject));
        return ctx;
    }

    /**
     * Finalize encode response.
     *
     * @param authnRequest the authn request
     * @param encoder      the encoder
     * @param samlResponse the saml response
     * @param relayState   the relay stateSurrogateAuthenticationPostProcessor.java
     * @throws Exception the saml exception
     */
    protected void finalizeEncode(final RequestAbstractType authnRequest,
                                  final BaseSAML2MessageEncoder encoder,
                                  final T samlResponse,
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
     */
    protected abstract BaseSAML2MessageEncoder getMessageEncoderInstance();
}
