package org.apereo.cas.support.saml.web.idp.profile.slo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;

/**
 * This is {@link SamlIdPHttpRedirectDeflateLogoutEncoder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
public class SamlIdPHttpRedirectDeflateLogoutEncoder extends HTTPRedirectDeflateEncoder {
    private final String endpointUrl;
    private final LogoutRequest logoutRequest;

    private String redirectUrl;

    @Override
    protected void doEncode() throws MessageEncodingException {
        val messageContext = new MessageContext();

        if (logoutRequest.isSigned()) {
            val signingContext = messageContext.getSubcontext(SecurityParametersContext.class, true);
            val signingParams = new SignatureSigningParameters();
            val signature = logoutRequest.getSignature();
            signingParams.setSigningCredential(signature.getSigningCredential());
            signingParams.setSignatureAlgorithm(signature.getSignatureAlgorithm());
            signingContext.setSignatureSigningParameters(signingParams);
        }

        removeSignature(logoutRequest);
        val encodedMessage = deflateAndBase64Encode(logoutRequest);
        messageContext.setMessage(logoutRequest);

        this.redirectUrl = buildRedirectURL(messageContext, endpointUrl, encodedMessage);
    }

    @Override
    protected void doInitialize() {
    }
}
