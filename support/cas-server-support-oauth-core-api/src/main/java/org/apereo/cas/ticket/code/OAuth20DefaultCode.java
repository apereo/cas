package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Entity
@Table(name = "OAUTH_TOKENS")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(OAuth20Code.PREFIX)
@NoArgsConstructor
@Getter
public class OAuth20DefaultCode extends AbstractTicket implements OAuth20Code {

    private static final long serialVersionUID = -8072724186202305800L;

    @Lob
    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>(0);

    @Lob
    @Column(name = "claims", length = Integer.MAX_VALUE)
    private HashMap<String, Map<String, Object>> claims = new HashMap<>(0);

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The service this ticket is valid for.
     */
    @Lob
    @Column(name = "SERVICE", nullable = false, length = Integer.MAX_VALUE)
    private Service service;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    @Lob
    @Column(name = "AUTHENTICATION", nullable = false, length = Integer.MAX_VALUE)
    private Authentication authentication;

    @Column(name = "code_challenge")
    private String codeChallenge;

    @Column(name = "code_challenge_method")
    private String codeChallengeMethod;

    @Column
    private String clientId;

    public OAuth20DefaultCode(final String id,
                              final @NonNull Service service,
                              final @NonNull Authentication authentication,
                              final @NonNull ExpirationPolicy expirationPolicy,
                              final TicketGrantingTicket ticketGrantingTicket,
                              final @NonNull Collection<String> scopes,
                              final String codeChallenge,
                              final String codeChallengeMethod,
                              final String clientId,
                              final Map<String, Map<String, Object>> requestClaims) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.clientId = clientId;
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
