package org.apereo.cas.support.saml.authentication.principal;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 6678711809842282833L;

    private final String relayState;

    private final String requestId;

    /**
     * Instantiates a new google accounts service.
     *
     * @param id         the id
     * @param relayState the relay state
     * @param requestId  the request id
     */
    protected GoogleAccountsService(final String id, final String relayState, final String requestId) {
        super(id, id, null);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    @JsonCreator
    public GoogleAccountsService(@JsonProperty("id") final String id,
                                 @JsonProperty("originalUrl") final String originalUrl,
                                 @JsonProperty("artifactId") final String artifactId,
                                 @JsonProperty("relayState") final String relayState,
                                 @JsonProperty("requestId") final String requestId) {
        super(id, originalUrl, artifactId);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    public String getRelayState() {
        return this.relayState;
    }

    public String getRequestId() {
        return this.requestId;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GoogleAccountsService rhs = (GoogleAccountsService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.relayState, rhs.relayState)
                .append(this.requestId, rhs.requestId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(relayState)
                .append(requestId)
                .toHashCode();
    }
}
