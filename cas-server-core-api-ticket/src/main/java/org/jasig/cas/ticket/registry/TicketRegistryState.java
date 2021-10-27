package org.jasig.cas.ticket.registry;

/**
 * Describes important state information that may be optionally exposed by
 * {@link org.jasig.cas.ticket.registry.TicketRegistry} components that might
 * be of interest to monitors.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public interface TicketRegistryState {
    /**
     * Computes the number of SSO sessions stored in the ticket registry.
     *
     * @return Number of ticket-granting tickets in the registry at time of invocation
     *         or {@link Integer#MIN_VALUE} if unknown.
     */
    int sessionCount();


    /**
     * Computes the number of service tickets stored in the ticket registry.
     *
     * @return Number of service tickets in the registry at time of invocation
     *         or {@link Integer#MIN_VALUE} if unknown.
     */
    int serviceTicketCount();
}
