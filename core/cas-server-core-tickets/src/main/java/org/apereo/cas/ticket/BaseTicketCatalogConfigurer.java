package org.apereo.cas.ticket;

/**
 * This is {@link BaseTicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseTicketCatalogConfigurer implements TicketCatalogConfigurer {
    /**
     * Build ticket ticket definition.
     *
     * @param plan   the plan
     * @param prefix the prefix
     * @param impl   the
     * @param order  the order
     * @return the ticket definition
     */
    protected TicketDefinition buildTicketDefinition(final TicketMetadataCatalog plan, final String prefix, final Class impl, final int order) {
        if (plan.containsTicketMetadata(prefix)) {
            return plan.findTicketMetadata(prefix);
        }
        return new DefaultTicketDefinition(impl, prefix, order);
    }

    /**
     * Register ticket definition.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    protected void registerTicketDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        plan.registerTicketMetadata(metadata);
    }

}
