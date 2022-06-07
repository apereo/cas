package org.apereo.cas.ticket;

import org.springframework.core.Ordered;

/**
 * This is {@link BaseTicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseTicketCatalogConfigurer implements TicketCatalogConfigurer {

    protected TicketDefinition buildTicketDefinition(final TicketCatalog plan, final String prefix,
                                                     final Class<? extends Ticket> api, final Class<? extends Ticket> impl, final int order) {
        if (plan.contains(prefix)) {
            return plan.find(prefix);
        }
        return new DefaultTicketDefinition(impl, api, prefix, order);
    }

    protected TicketDefinition buildTicketDefinition(final TicketCatalog plan, final String prefix, final Class impl, final Class api) {
        if (plan.contains(prefix)) {
            return plan.find(prefix);
        }
        return new DefaultTicketDefinition(impl, api, prefix, Ordered.LOWEST_PRECEDENCE);
    }
    
    protected void registerTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        plan.register(metadata);
    }

}
