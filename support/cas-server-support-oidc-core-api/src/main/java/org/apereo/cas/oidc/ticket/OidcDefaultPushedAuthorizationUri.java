package org.apereo.cas.oidc.ticket;

import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This is {@link OidcDefaultPushedAuthorizationUri}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@NoArgsConstructor(force = true)
public class OidcDefaultPushedAuthorizationUri extends AbstractTicket implements OidcPushedAuthorizationUri {
    private static final long serialVersionUID = 5050969039357176961L;

    @JsonIgnore
    private final String prefix = OidcPushedAuthorizationUri.PREFIX;

    private final String authorizationRequest;

    public OidcDefaultPushedAuthorizationUri(final String id,
                                             final ExpirationPolicy expirationPolicy,
                                             final String request) {
        super(id, expirationPolicy);
        this.authorizationRequest = request;
    }
}
