package org.apereo.cas.support.saml.web.idp.profile.slo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;

/**
 * This is {@link SamlIdPHttpRedirectDeflateEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
public class SamlIdPHttpRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {
    private final String endpointUrl;

    private final RequestAbstractType request;

    private String redirectUrl;

    private MessageContext messageContext;

    private String encodedRequest;

    @Override
    public void doEncode() throws MessageEncodingException {
        this.messageContext = new MessageContext();
        if (request.isSigned()) {
            val signingContext = messageContext.getSubcontext(SecurityParametersContext.class, true);
            val signingParams = new SignatureSigningParameters();
            val signature = request.getSignature();
            signingParams.setSigningCredential(signature.getSigningCredential());
            signingParams.setSignatureAlgorithm(signature.getSignatureAlgorithm());
            signingContext.setSignatureSigningParameters(signingParams);
        }

        removeSignature(request);
        encodedRequest = deflateAndBase64Encode(request);
        messageContext.setMessage(request);

        this.redirectUrl = buildRedirectURL(messageContext, endpointUrl, encodedRequest);
    }

    @Override
    protected void doInitialize() {
    }
}
