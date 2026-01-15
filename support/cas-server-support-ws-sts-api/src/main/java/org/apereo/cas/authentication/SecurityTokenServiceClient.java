package org.apereo.cas.authentication;

import module java.base;
import module java.xml;
import lombok.val;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link SecurityTokenServiceClient}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SecurityTokenServiceClient extends STSClient {
    public SecurityTokenServiceClient(final Bus bus) {
        super(bus);
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
        return requestSecurityTokenResponse(appliesTo, action, "/Issue");
    }

    private Element requestSecurityTokenResponse(final String appliesTo, @Nullable final String action,
                                                 final String requestType) throws Exception {
        val response = issue(appliesTo, action, requestType, null);
        return getDocumentElement(response.getResponse());
    }
}
