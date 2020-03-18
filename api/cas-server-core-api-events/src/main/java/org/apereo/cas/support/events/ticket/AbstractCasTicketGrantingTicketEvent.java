package org.apereo.cas.support.events.ticket;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;


/**
 * Abstract subclass of {@link AbstractCasEvent} encapsulating TGT and exposing key pieces attached to it. Useful for
 * all TGT-based events.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
@ToString(callSuper = true)
@Getter
public abstract class AbstractCasTicketGrantingTicketEvent extends AbstractCasEvent {

    public static final long serialVersionUID = 5815205609847140811L;

    private final TicketGrantingTicket ticketGrantingTicket;

    public AbstractCasTicketGrantingTicketEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket) {
        super(source);
        this.ticketGrantingTicket = ticketGrantingTicket;
    }

    /**
     * Get tgt creation time.
     *
     * @return tgt creation time
     */
    public ZonedDateTime getCreationTime() {
        return this.ticketGrantingTicket.getCreationTime();
    }

    /**
     * Get tgt id.
     *
     * @return tgt id
     */
    public String getId() {
        return this.ticketGrantingTicket.getId();
    }

    /**
     * Get tgt's TTL.
     *
     * @return tgt's TTL
     */
    public Long getTimeToLive() {
        return this.ticketGrantingTicket.getExpirationPolicy().getTimeToLive();
    }

    /**
     * Get tgt's TTI.
     *
     * @return tgt's TTI
     */
    public Long getTimeToIdle() {
        return this.ticketGrantingTicket.getExpirationPolicy().getTimeToIdle();
    }

    /**
     * Get principal id.
     *
     * @return principal id
     */
    public String getPrincipalId() {
        return this.ticketGrantingTicket.getAuthentication().getPrincipal().getId();
    }
}
