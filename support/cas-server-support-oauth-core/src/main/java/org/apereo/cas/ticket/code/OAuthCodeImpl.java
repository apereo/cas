package org.apereo.cas.ticket.code;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import lombok.NoArgsConstructor;

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
@Slf4j
@NoArgsConstructor
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
    @Column(name = "AUTHENTICATION", nullable = false, length = 1000000)
    private Authentication authentication;

    /**
     * Constructs a new OAuth code with unique id for a service and authentication.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param authentication       the authentication.
     * @param expirationPolicy     the expiration policy.
     * @param ticketGrantingTicket the ticket granting ticket
     * @param scopes               the scopes
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public OAuthCodeImpl(final String id, @NonNull final Service service, @NonNull final Authentication authentication, final ExpirationPolicy expirationPolicy,
                         final TicketGrantingTicket ticketGrantingTicket, final Collection<String> scopes) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.scopes.addAll(scopes);
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public Service getService() {
        return this.service;
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
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
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
