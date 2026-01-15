package org.apereo.cas.ticket.code;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.BaseOAuth20Token;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * An OAuth code implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@NoArgsConstructor
public class OAuth20DefaultCode extends BaseOAuth20Token implements OAuth20Code {
    @Serial
    private static final long serialVersionUID = -8203878835348247880L;

    public OAuth20DefaultCode(final String id,
                              final @NonNull Service service,
                              final @NonNull Authentication authentication,
                              final @NonNull ExpirationPolicy expirationPolicy,
                              final Ticket ticketGrantingTicket,
                              @JsonSetter(nulls = Nulls.AS_EMPTY)
                              final Collection<String> scopes,
                              final String codeChallenge,
                              final String codeChallengeMethod,
                              final String clientId,
                              @JsonSetter(nulls = Nulls.AS_EMPTY)
                              final Map<String, Map<String, Object>> requestClaims,
                              final OAuth20ResponseTypes responseType,
                              final OAuth20GrantTypes grantType) {
        super(id, service, authentication, expirationPolicy, ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod, clientId, requestClaims, responseType, grantType);
    }
}
