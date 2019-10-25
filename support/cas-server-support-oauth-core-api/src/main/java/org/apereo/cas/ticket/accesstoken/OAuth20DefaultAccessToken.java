package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collection;
import java.util.Map;

/**
 * An OAuth access token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(OAuth20AccessToken.PREFIX)
@NoArgsConstructor
@Setter
@Getter
public class OAuth20DefaultAccessToken extends OAuth20DefaultCode implements OAuth20AccessToken {

    private static final long serialVersionUID = 2339545346159721563L;

    @Column(length = 2048)
    private String idToken;

    public OAuth20DefaultAccessToken(final String id, final Service service,
                                     final Authentication authentication,
                                     final ExpirationPolicy expirationPolicy,
                                     final TicketGrantingTicket ticketGrantingTicket,
                                     final Collection<String> scopes,
                                     final String codeChallenge,
                                     final String codeChallengeMethod,
                                     final String clientId,
                                     final Map<String, Map<String, Object>> requestClaims) {
        super(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod,
            clientId, requestClaims);
    }

    public OAuth20DefaultAccessToken(final String id, final Service service,
                                     final Authentication authentication,
                                     final ExpirationPolicy expirationPolicy,
                                     final TicketGrantingTicket ticketGrantingTicket,
                                     final Collection<String> scopes,
                                     final String clientId,
                                     final Map<String, Map<String, Object>> requestClaims) {
        this(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes, null,
            null, clientId, requestClaims);
    }

    @Override
    public String getPrefix() {
        return OAuth20AccessToken.PREFIX;
    }
}
