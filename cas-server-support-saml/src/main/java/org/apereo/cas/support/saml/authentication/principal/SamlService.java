package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.WebApplicationService;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class SamlService extends AbstractWebApplicationService {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    private String requestId;


    /**
     * Instantiates a new SAML service.
     *
     * @param id the service id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param requestId the request id
     * @param responseBuilder the response builder
     */
    protected SamlService(final String id, final String originalUrl,
                          final String artifactId, final String requestId,
                          final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, originalUrl, artifactId, responseBuilder);
        this.requestId = requestId;
    }

    public String getRequestID() {
        return this.requestId;
    }
}
