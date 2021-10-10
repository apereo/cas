package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

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
public class OAuth20DefaultCode extends AbstractTicket implements OAuth20Code {

    private static final long serialVersionUID = -8072724186202305800L;

    private Set<String> scopes = new HashSet<>(0);

    private Map<String, Map<String, Object>> claims = new HashMap<>(0);

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

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
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id,
                                                        final Authentication authentication,
                                                        final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No proxy-granting ticket can be issued");
    }

    @Override
    public String getPrefix() {
        return OAuth20Code.PREFIX;
    }

    @Override
    public Set<String> getScopes() {
        return ObjectUtils.defaultIfNull(this.scopes, new HashSet<>(0));
    }
}
