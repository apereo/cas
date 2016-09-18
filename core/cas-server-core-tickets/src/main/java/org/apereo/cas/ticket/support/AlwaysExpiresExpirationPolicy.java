package org.apereo.cas.ticket.support;

import org.apereo.cas.ticket.TicketState;

/**
 * AlwaysExpiresExpirationPolicy always answers true when asked if a Ticket is
 * expired.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class AlwaysExpiresExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serializable Unique ID. */
    private static final long serialVersionUID = 3836547698242303540L;

    /**
     * Instantiates a new Always expires expiration policy.
     */
    public AlwaysExpiresExpirationPolicy() {}

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return true;
    }

    @Override
    public Long getTimeToLive() {
        return 0L;
    }

    @Override
    public Long getTimeToIdle() {
        return 0L;
    }
}
