package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 *
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated Since 6.2, to be replaced with CAS SAML2 identity provider functionality.
 */
@Entity
@DiscriminatorValue("google")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Deprecated(since = "6.2.0")
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 6678711809842282833L;

    @Column
    private String relayState;

    @Column
    private String requestId;

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
    public GoogleAccountsService(@JsonProperty("id") final String id, @JsonProperty("originalUrl") final String originalUrl,
                                 @JsonProperty("artifactId") final String artifactId, @JsonProperty("relayState") final String relayState,
                                 @JsonProperty("requestId") final String requestId) {
        super(id, originalUrl, artifactId);
        this.relayState = relayState;
        this.requestId = requestId;
    }
}
