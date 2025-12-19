package org.apereo.cas.support.x509;

import module java.base;
import module java.xml;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.delegation.TokenDelegationHandler;
import org.apache.cxf.sts.token.delegation.TokenDelegationParameters;
import org.apache.cxf.sts.token.delegation.TokenDelegationResponse;
import org.apache.wss4j.common.WSS4JConstants;

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
        val token = delegateTarget.getToken();
        if (token instanceof final Element tokenElement) {
            val namespace = tokenElement.getNamespaceURI();
            val localname = tokenElement.getLocalName();
            return WSS4JConstants.SIG_NS.equals(namespace) && WSS4JConstants.X509_DATA_LN.equals(localname);
        }
        return false;
    }

    @Override
    public TokenDelegationResponse isDelegationAllowed(final TokenDelegationParameters tokenParameters) {
        val response = new TokenDelegationResponse();
        val delegateTarget = tokenParameters.getToken();
        response.setToken(delegateTarget);

        if (!delegateTarget.isDOMElement()) {
            return response;
        }

        if (delegateTarget.getState() == ReceivedToken.STATE.VALID && delegateTarget.getPrincipal() != null) {
            response.setDelegationAllowed(true);
            LOGGER.debug("Delegation is allowed for [{}]", delegateTarget.getPrincipal());
        } else {
            LOGGER.debug("Delegation is not allowed; token is invalid or the principal is undefined");
        }
        return response;
    }

}
