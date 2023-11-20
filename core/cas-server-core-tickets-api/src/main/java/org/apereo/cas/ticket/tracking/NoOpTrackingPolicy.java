package org.apereo.cas.ticket.tracking;

/**
 * This is a no-operation ticket tracking policy.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public final class NoOpTrackingPolicy implements TicketTrackingPolicy {

    /**
     * A single instance of this class.
     */
    public static final TicketTrackingPolicy INSTANCE = new NoOpTrackingPolicy();

    private NoOpTrackingPolicy() {}
}
