package org.apereo.cas.support.x509;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.delegation.TokenDelegationHandler;
import org.apache.cxf.sts.token.delegation.TokenDelegationParameters;
import org.apache.cxf.sts.token.delegation.TokenDelegationResponse;
import org.apache.wss4j.dom.WSConstants;
import org.w3c.dom.Element;

/**
 * This is {@link X509TokenDelegationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class X509TokenDelegationHandler implements TokenDelegationHandler {
    @Override
    public boolean canHandleToken(final ReceivedToken delegateTarget) {
        final Object token = delegateTarget.getToken();
        if (token instanceof Element) {
            final Element tokenElement = (Element) token;
            final String namespace = tokenElement.getNamespaceURI();
            final String localname = tokenElement.getLocalName();
            return WSConstants.SIG_NS.equals(namespace) && WSConstants.X509_DATA_LN.equals(localname);
        }
        return false;
    }

    @Override
    public TokenDelegationResponse isDelegationAllowed(final TokenDelegationParameters tokenParameters) {
        final TokenDelegationResponse response = new TokenDelegationResponse();
        final ReceivedToken delegateTarget = tokenParameters.getToken();
        response.setToken(delegateTarget);

        if (!delegateTarget.isDOMElement()) {
            return response;
        }

        if (delegateTarget.getState() == ReceivedToken.STATE.VALID && delegateTarget.getPrincipal() != null) {
            response.setDelegationAllowed(true);
            LOGGER.debug("Delegation is allowed for: [{}]", delegateTarget.getPrincipal());
        } else {
            LOGGER.debug("Delegation is not allowed, as the token is invalid or the principal is null");
        }

        return response;
    }

}
