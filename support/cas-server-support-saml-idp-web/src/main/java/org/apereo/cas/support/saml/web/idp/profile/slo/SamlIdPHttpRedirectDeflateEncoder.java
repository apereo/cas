package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import java.util.Objects;

/**
 * This is {@link SamlIdPHttpRedirectDeflateEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class SamlIdPHttpRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {
    private final String endpointUrl;

    private final SignableXMLObject request;

    private String redirectUrl;

    private MessageContext messageContext;

    private String encodedRequest;

    @Setter
    private String relayState;

    @Override
    public void doEncode() throws MessageEncodingException {
        this.messageContext = new MessageContext();
        if (request.isSigned()) {
            LOGGER.trace("Request is signed for [{}]", request.getElementQName());
            val signingContext = messageContext.getSubcontext(SecurityParametersContext.class, true);
            val signingParams = new SignatureSigningParameters();
            val signature = request.getSignature();
            signingParams.setSigningCredential(Objects.requireNonNull(signature).getSigningCredential());
            signingParams.setSignatureAlgorithm(signature.getSignatureAlgorithm());
            Objects.requireNonNull(signingContext).setSignatureSigningParameters(signingParams);
        }

        val samlObject = SAMLObject.class.cast(request);
        removeSignature(samlObject);
        encodedRequest = deflateAndBase64Encode(samlObject);
        messageContext.setMessage(request);
        FunctionUtils.doIfNotNull(relayState, value -> SAMLBindingSupport.setRelayState(messageContext, value));

        this.redirectUrl = buildRedirectURL(messageContext, endpointUrl, encodedRequest);
        LOGGER.debug("Created redirect URL [{}] based on endpoint [{}]", this.redirectUrl, endpointUrl);
    }

    @Override
    protected void doInitialize() {
    }
}
