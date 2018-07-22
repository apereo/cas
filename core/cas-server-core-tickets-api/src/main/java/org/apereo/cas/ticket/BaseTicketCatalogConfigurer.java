package org.apereo.cas.ticket;

import org.springframework.core.Ordered;

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
    protected TicketDefinition buildTicketDefinition(final TicketCatalog plan, final String prefix, final Class impl, final int order) {
        if (plan.contains(prefix)) {
            return plan.find(prefix);
        }
        return new DefaultTicketDefinition(impl, prefix, order);
    }

    /**
     * Build ticket definition ticket.
     *
     * @param plan   the plan
     * @param prefix the prefix
     * @param impl   the
     * @return the ticket definition
     */
    protected TicketDefinition buildTicketDefinition(final TicketCatalog plan, final String prefix, final Class impl) {
        if (plan.contains(prefix)) {
            return plan.find(prefix);
        }
        return new DefaultTicketDefinition(impl, prefix, Ordered.LOWEST_PRECEDENCE);
    }

    /**
     * Register ticket definition.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    protected void registerTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        plan.register(metadata);
    }

}
