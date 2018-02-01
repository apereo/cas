package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Domain object representing a Service Ticket. A service ticket grants specific
 * access to a particular service. It will only work for a particular service.
 * Generally, it is a one time use Ticket, but the specific expiration policy
 * can be anything.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Entity
@Table(name = "SERVICETICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(ServiceTicket.PREFIX)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@Setter
@NoArgsConstructor
@Getter
public class ServiceTicketImpl extends AbstractTicket implements ServiceTicket {

    private static final long serialVersionUID = -4223319704861765405L;

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The service this ticket is valid for.
     */
    @Lob
    @Column(name = "SERVICE", nullable = false, length = Integer.MAX_VALUE)
    private Service service;

    /**
     * Is this service ticket the result of a new login?
     */
    @Column(name = "FROM_NEW_LOGIN", nullable = false)
    private boolean fromNewLogin;

    @Column(name = "TICKET_ALREADY_GRANTED", nullable = false)
    private Boolean grantedTicketAlready = Boolean.FALSE;

    /**
     * Constructs a new ServiceTicket with a Unique Id, a TicketGrantingTicket,
     * a Service, Expiration Policy and a flag to determine if the ticket
     * creation was from a new Login or not.
     *
     * @param id                 the unique identifier for the ticket.
     * @param ticket             the TicketGrantingTicket parent.
     * @param service            the service this ticket is for.
     * @param credentialProvided current credential that prompted this service ticket. May be null.
     * @param policy             the expiration policy for the Ticket.
     * @throws IllegalArgumentException if the TicketGrantingTicket or the Service are null.
     */
    @JsonCreator
    public ServiceTicketImpl(@JsonProperty("id") final String id, @NonNull @JsonProperty("ticketGrantingTicket") final TicketGrantingTicket ticket,
                             @NonNull @JsonProperty("service") final Service service, @JsonProperty("credentialProvided") final boolean credentialProvided,
                             @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {
        super(id, policy);
        this.ticketGrantingTicket = ticket;
        this.service = service;
        this.fromNewLogin = credentialProvided || ticket.getCountOfUses() == 0;
    }

    /**
     * {@inheritDoc}
     * <p>The state of the ticket is affected by this operation and the
     * ticket will be considered used regardless of the match result.
     * The state update subsequently may impact the ticket expiration
     * policy in that, depending on the policy configuration, the ticket
     * may be considered expired.
     */
    @Override
    public boolean isValidFor(final Service serviceToValidate) {
        update();
        return serviceToValidate.matches(this.service);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) throws AbstractTicketException {
        if (this.grantedTicketAlready) {
            LOGGER.warn("Service ticket [{}] issued for service [{}] has already allotted a proxy-granting ticket", getId(), this.service.getId());
            throw new InvalidProxyGrantingTicketForServiceTicketException(this.service);
        }
        this.grantedTicketAlready = Boolean.TRUE;
        final ProxyGrantingTicket pgt = new ProxyGrantingTicketImpl(id, this.service, this.getTicketGrantingTicket(), authentication, expirationPolicy);
        getTicketGrantingTicket().getProxyGrantingTickets().put(pgt.getId(), this.service);
        return pgt;
    }

    @Override
    public Authentication getAuthentication() {
        return getTicketGrantingTicket().getAuthentication();
    }

    @Override
    public String getPrefix() {
        return ServiceTicket.PREFIX;
    }
}
