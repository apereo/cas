package org.apereo.cas.ticket;

/**
 * This is {@link TicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface TicketCatalogConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureTicketCatalog(TicketCatalog plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
