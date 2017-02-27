package org.apereo.cas.ticket;

/**
 * This is {@link TicketMetadataRegistrationConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataRegistrationConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureTicketMetadataRegistrationPlan(final TicketMetadataCatalogRegistrationPlan plan) {
    }
}
