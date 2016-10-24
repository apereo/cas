package org.apereo.cas.support.saml.authentication.principal;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationService;

/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 6678711809842282833L;

    private String relayState;

    private String requestId;

    /**
     * Instantiates a new google accounts service.
     *
     * @param id              the id
     * @param relayState      the relay state
     * @param requestId       the request id
     * @param responseBuilder the response builder
     */
    protected GoogleAccountsService(final String id, final String relayState, final String requestId,
                                    final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, id, null, responseBuilder);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    @JsonCreator
    public GoogleAccountsService(@JsonProperty("id") String id, @JsonProperty("originalUrl") String originalUrl, @JsonProperty("artifactId") String artifactId, @JsonProperty("responseBuilder") ResponseBuilder<WebApplicationService> responseBuilder,
                                 @JsonProperty("relayState") String relayState, @JsonProperty("requestId") String requestId) {
        super(id, originalUrl, artifactId, responseBuilder);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    /**
     * Return true if the service is already logged out.
     *
     * @return true if the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return true;
    }

    public String getRelayState() {
        return this.relayState;
    }

    public String getRequestId() {
        return this.requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GoogleAccountsService that = (GoogleAccountsService) o;

        if (relayState != null ? !relayState.equals(that.relayState) : that.relayState != null) return false;
        return requestId != null ? requestId.equals(that.requestId) : that.requestId == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (relayState != null ? relayState.hashCode() : 0);
        result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
        return result;
    }
}
