package org.apereo.cas.ticket;

/**
 * This is {@link TicketMetadataCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataCatalogConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureTicketMetadataCatalog(final TicketMetadataCatalog plan) {
    }
}
