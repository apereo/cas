package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.BaseOAuth20Token;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;

/**
 * An OAuth code implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@NoArgsConstructor
public class OAuth20DefaultCode extends BaseOAuth20Token implements OAuth20Code {
    private static final long serialVersionUID = -8203878835348247880L;

    public OAuth20DefaultCode(final String id,
                              final @NonNull Service service,
                              final @NonNull Authentication authentication,
                              final @NonNull ExpirationPolicy expirationPolicy,
                              final TicketGrantingTicket ticketGrantingTicket,
                              final @NonNull Collection<String> scopes,
                              final String codeChallenge,
                              final String codeChallengeMethod,
                              final String clientId,
                              final Map<String, Map<String, Object>> requestClaims,
                              final OAuth20ResponseTypes responseType,
                              final OAuth20GrantTypes grantType) {
        super(id, service, authentication, expirationPolicy, ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod, clientId, requestClaims, responseType, grantType);
    }
}
