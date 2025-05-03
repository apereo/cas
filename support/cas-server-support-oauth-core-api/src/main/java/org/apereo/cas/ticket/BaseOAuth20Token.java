package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.code.OAuth20Code;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An OAuth code implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@NoArgsConstructor
@Getter
public abstract class BaseOAuth20Token extends AbstractTicket implements OAuth20Token {

    @Serial
    private static final long serialVersionUID = -8072724186202305800L;

    private Set<String> scopes = new HashSet<>();

    private Map<String, Map<String, Object>> claims = new HashMap<>();

    private Ticket ticketGrantingTicket;

    /**
     * The service this ticket is valid for.
     */
    private Service service;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    private Authentication authentication;

    private String codeChallenge;

    private String codeChallengeMethod;

    private String clientId;

    private OAuth20ResponseTypes responseType;

    private OAuth20GrantTypes grantType;

    public BaseOAuth20Token(final String id,
                            final @NonNull Service service,
                            final Authentication authentication,
                            final @NonNull ExpirationPolicy expirationPolicy,
                            final Ticket ticketGrantingTicket,
                            final @NonNull Collection<String> scopes,
                            final String codeChallenge,
                            final String codeChallengeMethod,
                            final String clientId,
                            final Map<String, Map<String, Object>> requestClaims,
                            final OAuth20ResponseTypes responseType,
                            final OAuth20GrantTypes grantType) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.clientId = clientId;
        this.responseType = responseType;
        this.grantType = grantType;
        this.scopes.addAll(scopes);
        this.claims.putAll(requestClaims);
    }

    @Override
    public String getPrefix() {
        return OAuth20Code.PREFIX;
    }

    @Override
    public Set<String> getScopes() {
        return ObjectUtils.defaultIfNull(this.scopes, new HashSet<>());
    }
}
