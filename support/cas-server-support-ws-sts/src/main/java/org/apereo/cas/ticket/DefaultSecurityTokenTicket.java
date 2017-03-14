package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This is {@link DefaultSecurityTokenTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Table(name = "SECURITYTOKENTICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(SecurityTokenTicket.PREFIX)
public class DefaultSecurityTokenTicket extends AbstractTicket implements SecurityTokenTicket {

    private static final long serialVersionUID = 3940671352560102114L;

    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    private String securityToken;
    
    public DefaultSecurityTokenTicket() {
    }

    public DefaultSecurityTokenTicket(final String id,
                                      final TicketGrantingTicket ticketGrantingTicket,
                                      final ExpirationPolicy expirationPolicy,
                                      final String securityToken) {
        super(id, expirationPolicy);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.securityToken = securityToken;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public Authentication getAuthentication() {
        return getGrantingTicket().getAuthentication();
    }

    @Override
    public String getPrefix() {
        return SecurityTokenTicket.PREFIX;
    }
}
