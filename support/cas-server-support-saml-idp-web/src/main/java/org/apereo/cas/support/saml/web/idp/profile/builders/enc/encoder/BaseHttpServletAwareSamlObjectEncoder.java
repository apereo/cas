package org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseHttpServletAwareSamlObjectEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseHttpServletAwareSamlObjectEncoder<T extends SAMLObject> {
    /**
     * The Velocity engine factory.
     */
    protected final VelocityEngine velocityEngineFactory;

    /**
     * The Adaptor.
     */
    protected final SamlRegisteredServiceMetadataAdaptor adaptor;

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
     * @param request        the request
     * @param samlObject     the saml response
     * @param relayState     the relay state
     * @param messageContext the message context
     * @return the response
     * @throws SamlException the saml exception
     */
    public final T encode(final RequestAbstractType request, final T samlObject,
                          final String relayState,
                          final MessageContext messageContext) throws SamlException {
        if (httpResponse != null) {
            val encoder = getMessageEncoderInstance();
            encoder.setHttpServletResponseSupplier(() -> httpResponse);

            val ctx = getEncoderMessageContext(request, samlObject, relayState, messageContext);
            encoder.setMessageContext(ctx);
            finalizeEncode(request, encoder, samlObject, relayState, messageContext);
        }
        return samlObject;

    }


    /**
     * Build encoder message context.
     *
     * @param request        the authn request
     * @param samlObject     the saml response
     * @param relayState     the relay state
     * @param messageContext the message context
     * @return the message context
     */
    protected MessageContext getEncoderMessageContext(final RequestAbstractType request, final T samlObject,
                                                      final String relayState, final MessageContext messageContext) {
        val ctx = new MessageContext();
        ctx.setMessage(samlObject);
        ctx.addSubcontext(messageContext.ensureSubcontext(SAMLBindingContext.class));
        SAMLBindingSupport.setRelayState(ctx, relayState);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(Pair.of(request, messageContext), ctx, adaptor, getBinding());
        val self = ctx.ensureSubcontext(SAMLSelfEntityContext.class);
        self.setEntityId(SamlIdPUtils.getIssuerFromSamlObject(samlObject));
        return ctx;
    }

    protected void finalizeEncode(final RequestAbstractType authnRequest,
                                  final BaseSAML2MessageEncoder encoder,
                                  final T samlResponse,
                                  final String relayState,
                                  final MessageContext messageContext) {
        FunctionUtils.doUnchecked(__ -> {
            encoder.initialize();
            encoder.encode();
        });
    }

    protected abstract String getBinding();

    /**
     * Gets message encoder instance.
     *
     * @return the message encoder instance
     */
    protected abstract BaseSAML2MessageEncoder getMessageEncoderInstance();
}
