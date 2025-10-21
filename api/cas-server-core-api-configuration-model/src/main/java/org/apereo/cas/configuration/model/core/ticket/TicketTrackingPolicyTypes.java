package org.apereo.cas.configuration.model.core.ticket;

/**
 * This is {@link TicketTrackingPolicyTypes}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public enum TicketTrackingPolicyTypes {
    /**
     * Only track the most recent ticket.
     */
    MOST_RECENT,
    /**
     * Track all tickets.
     */
    ALL
}
