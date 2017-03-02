package org.apereo.cas.ticket;

/**
 * This is {@link TicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketCatalogConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureTicketCatalog(final TicketCatalog plan) {
    }
}
