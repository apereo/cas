package org.apereo.cas.authentication;

import lombok.val;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.w3c.dom.Element;

/**
 * This is {@link SecurityTokenServiceClient}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceClient extends STSClient {
    public SecurityTokenServiceClient(final Bus b) {
        super(b);
    }

    /**
     * Request security token response element.
     *
     * @param appliesTo the applies to
     * @return the element
     * @throws Exception the exception
     */
    public Element requestSecurityTokenResponse(final String appliesTo) throws Exception {
        val action = isSecureConv ? namespace + "/RST/SCT" : null;
        return requestSecurityTokenResponse(appliesTo, action, "/Issue", null);
    }

    /**
     * Request security token response element.
     *
     * @param appliesTo   the applies to
     * @param action      the action
     * @param requestType the request type
     * @param target      the target
     * @return the element
     * @throws Exception the exception
     */
    public Element requestSecurityTokenResponse(final String appliesTo, final String action,
                                                final String requestType, final SecurityToken target) throws Exception {
        val response = issue(appliesTo, action, "/Issue", null);
        return getDocumentElement(response.getResponse());
    }
}
