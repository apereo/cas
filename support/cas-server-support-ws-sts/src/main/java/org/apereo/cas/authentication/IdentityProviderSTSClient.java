package org.apereo.cas.authentication;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.w3c.dom.Element;

/**
 * This is {@link IdentityProviderSTSClient}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class IdentityProviderSTSClient extends STSClient {
    public IdentityProviderSTSClient(final Bus b) {
        super(b);
    }

    public Element requestSecurityTokenResponse(final String appliesTo) throws Exception {
        String action = null;
        if (isSecureConv) {
            action = namespace + "/RST/SCT";
        }
        return requestSecurityTokenResponse(appliesTo, action, "/Issue", null);
    }

    public Element requestSecurityTokenResponse(final String appliesTo, final String action,
                                                final String requestType, final SecurityToken target) throws Exception {
        final STSResponse response = issue(appliesTo, null, "/Issue", null);
        return getDocumentElement(response.getResponse());
    }
}
