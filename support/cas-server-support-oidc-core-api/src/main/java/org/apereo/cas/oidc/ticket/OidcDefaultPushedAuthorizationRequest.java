package org.apereo.cas.oidc.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This is {@link OidcDefaultPushedAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@NoArgsConstructor(force = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class OidcDefaultPushedAuthorizationRequest extends AbstractTicket
    implements OidcPushedAuthorizationRequest {
    private static final long serialVersionUID = 5050969039357176961L;

    @JsonIgnore
    private final String prefix = OidcPushedAuthorizationRequest.PREFIX;

    private final Authentication authentication;

    private final Service service;

    private final OAuthRegisteredService registeredService;

    private final String authorizationRequest;

    public OidcDefaultPushedAuthorizationRequest(final String id,
                                                 final ExpirationPolicy expirationPolicy,
                                                 final Authentication authentication,
                                                 final Service service,
                                                 final OAuthRegisteredService registeredService,
                                                 final String request) {
        super(id, expirationPolicy);
        this.authorizationRequest = request;
        this.authentication = authentication;
        this.service = service;
        this.registeredService = registeredService;
    }
}
