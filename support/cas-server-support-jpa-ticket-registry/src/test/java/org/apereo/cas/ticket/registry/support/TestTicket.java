package org.apereo.cas.ticket.registry.support;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.NoArgsConstructor;

/**
 * Mock ticket type for testing ticket types outside this project can be deleted
 * too.
 *
 * @author sbearcsiro
 * @since 5.2.3
 *
 */
@Entity
@Table(name = "TEST")
@NoArgsConstructor
public class TestTicket extends AbstractTicket {

    public static final String PREFIX = "TEST";

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TicketGrantingTicket ticketGrantingTicket;

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return ticketGrantingTicket;
    }

    public void setTicketGrantingTicket(final TicketGrantingTicket ticketGrantingTicket) {
        this.ticketGrantingTicket = ticketGrantingTicket;
    }

    @Override
    public Authentication getAuthentication() {
        return getTicketGrantingTicket().getAuthentication();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

}
