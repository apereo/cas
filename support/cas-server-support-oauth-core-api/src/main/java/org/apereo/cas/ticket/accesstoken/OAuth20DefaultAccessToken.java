package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.BaseOAuth20Token;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * An OAuth access token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@NoArgsConstructor
public class OAuth20DefaultAccessToken extends BaseOAuth20Token implements OAuth20AccessToken {

    @Serial
    private static final long serialVersionUID = 2339545346159721563L;

    @Setter
    @Getter
    private String idToken;

    @Getter
    private String token;

    public OAuth20DefaultAccessToken(final String id,
                                     final Service service,
                                     final Authentication authentication,
                                     final ExpirationPolicy expirationPolicy,
                                     final Ticket ticketGrantingTicket,
                                     final String token,
                                     final Collection<String> scopes,
                                     final String codeChallenge,
                                     final String codeChallengeMethod,
                                     final String clientId,
                                     final Map<String, Map<String, Object>> requestClaims,
                                     final OAuth20ResponseTypes responseType,
                                     final OAuth20GrantTypes grantType) {
        super(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod,
            clientId, requestClaims, responseType, grantType);
        this.token = token;
    }

    public OAuth20DefaultAccessToken(final String id,
                                     final Service service,
                                     final Authentication authentication,
                                     final ExpirationPolicy expirationPolicy,
                                     final Ticket ticketGrantingTicket,
                                     final String token,
                                     final Collection<String> scopes,
                                     final String clientId,
                                     final Map<String, Map<String, Object>> requestClaims,
                                     final OAuth20ResponseTypes responseType,
                                     final OAuth20GrantTypes grantType) {
        this(id, service, authentication, expirationPolicy,
            ticketGrantingTicket, token, scopes, null,
            null, clientId, requestClaims, responseType, grantType);
    }

    @Override
    public String getPrefix() {
        return OAuth20AccessToken.PREFIX;
    }

    @Override
    public long getExpiresIn() {
        return getExpirationPolicy().getTimeToLive();
    }
}
