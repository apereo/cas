package org.apereo.cas.support.saml.authentication.principal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

/**
 * Class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
     */
    @JsonCreator
    protected SamlService(@JsonProperty("id") final String id, @JsonProperty("originalUrl") final String originalUrl,
                          @JsonProperty("artifactId") final String artifactId, @JsonProperty("requestID") final String requestId) {
        super(id, originalUrl, artifactId);
        this.requestId = requestId;
    }

    public String getRequestID() {
        return this.requestId;
    }
}
