package org.jasig.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
@Table(name="SERVICETICKET")
@DiscriminatorColumn(name="TYPE")
@DiscriminatorValue(ServiceTicket.PREFIX)
public class ServiceTicketImpl extends AbstractTicket implements ServiceTicket {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4223319704861765405L;

    /** The service this ticket is valid for. */
    @Lob
    @Column(name="SERVICE", nullable=false, length = Integer.MAX_VALUE)
    private Service service;

    /** Is this service ticket the result of a new login. */
    @Column(name="FROM_NEW_LOGIN", nullable=false)
    private boolean fromNewLogin;

    @Column(name="TICKET_ALREADY_GRANTED", nullable=false)
    private Boolean grantedTicketAlready = Boolean.FALSE;

    private final transient Object lock = new Object();

    /**
     * Instantiates a new service ticket impl.
     */
    public ServiceTicketImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new ServiceTicket with a Unique Id, a TicketGrantingTicket,
     * a Service, Expiration Policy and a flag to determine if the ticket
     * creation was from a new Login or not.
     *
     * @param id the unique identifier for the ticket.
     * @param ticket the TicketGrantingTicket parent.
     * @param service the service this ticket is for.
     * @param fromNewLogin is it from a new login.
     * @param policy the expiration policy for the Ticket.
     * @throws IllegalArgumentException if the TicketGrantingTicket or the
     * Service are null.
     */
    public ServiceTicketImpl(final String id,
        @NotNull final TicketGrantingTicketImpl ticket, @NotNull final Service service,
        final boolean fromNewLogin, final ExpirationPolicy policy) {
        super(id, ticket, policy);

        Assert.notNull(service, "service cannot be null");
        Assert.notNull(ticket, "ticket cannot be null");
        this.service = service;
        this.fromNewLogin = fromNewLogin;
    }

    @Override
    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    @Override
    public Service getService() {
        return this.service;
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
        updateState();
        return serviceToValidate.matches(this.service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof ServiceTicket)) {
            return false;
        }

        final Ticket ticket = (Ticket) object;

        return new EqualsBuilder()
                .append(ticket.getId(), this.getId())
                .isEquals();
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(
        final String id, final Authentication authentication,
        final ExpirationPolicy expirationPolicy) {
        synchronized (this.lock) {
            if(this.grantedTicketAlready) {
                throw new IllegalStateException(
                    "PGT already generated for this ST. Cannot grant more than one TGT for ST");
            }
            this.grantedTicketAlready = Boolean.TRUE;
        }

        return new ProxyGrantingTicketImpl(id, service,
                this.getGrantingTicket(), authentication, expirationPolicy);
    }

    @Override
    public Authentication getAuthentication() {
        return null;
    }

}
