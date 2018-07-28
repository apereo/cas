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
import java.util.HashSet;

/**
 * An OAuth code implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@Table(name = "OAUTH_TOKENS")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(OAuthCode.PREFIX)
@NoArgsConstructor
@Getter
public class OAuthCodeImpl extends AbstractTicket implements OAuthCode {

    private static final long serialVersionUID = -8072724186202305800L;

    @Lob
    @Column(name = "scopes", length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>();

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
    @Column(name = "SERVICE", nullable = false)
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

    public OAuthCodeImpl(final String id,
                         @NonNull final Service service,
                         @NonNull final Authentication authentication,
                         final ExpirationPolicy expirationPolicy,
                         final TicketGrantingTicket ticketGrantingTicket,
                         final Collection<String> scopes,
                         final String codeChallenge,
                         final String codeChallengeMethod) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.scopes.addAll(scopes);
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public boolean isValidFor(final Service serviceToValidate) {
        update();
        return serviceToValidate.matches(this.service);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No PGT grant is available in OAuth");
    }

    @Override
    public String getPrefix() {
        return OAuthCode.PREFIX;
    }

    @Override
    public Collection<String> getScopes() {
        return ObjectUtils.defaultIfNull(this.scopes, new HashSet<>());
    }
}
