package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * AlwaysExpiresExpirationPolicy always answers true when asked if a Ticket is
 * expired.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("alwaysExpiresExpirationPolicy")
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
