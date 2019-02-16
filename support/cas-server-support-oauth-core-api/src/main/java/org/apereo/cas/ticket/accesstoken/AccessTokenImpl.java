package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuthCodeImpl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collection;

/**
 * An OAuth access token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(AccessToken.PREFIX)
@NoArgsConstructor
@Setter
@Getter
public class AccessTokenImpl extends OAuthCodeImpl implements AccessToken {

    private static final long serialVersionUID = 2339545346159721563L;

    @Column
    private String idToken;

    public AccessTokenImpl(final String id, final Service service,
                           final Authentication authentication,
                           final ExpirationPolicy expirationPolicy,
                           final TicketGrantingTicket ticketGrantingTicket,
                           final Collection<String> scopes,
                           final String codeChallenge,
                           final String codeChallengeMethod) {
        super(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes, codeChallenge, codeChallengeMethod);
    }

    public AccessTokenImpl(final String id, final Service service,
                           final Authentication authentication,
                           final ExpirationPolicy expirationPolicy,
                           final TicketGrantingTicket ticketGrantingTicket,
                           final Collection<String> scopes) {
        this(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes, null, null);
    }

    @Override
    public String getPrefix() {
        return AccessToken.PREFIX;
    }
}
