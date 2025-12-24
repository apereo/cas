package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
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
            return Objects.requireNonNull(plan.find(prefix));
        }
        return new DefaultTicketDefinition(impl, api, prefix, order);
    }

    protected TicketDefinition buildTicketDefinition(final TicketCatalog plan, final String prefix,
                                                     final Class impl, final Class api) throws Throwable {
        if (plan.contains(prefix)) {
            return Objects.requireNonNull(plan.find(prefix));
        }
        FunctionUtils.throwIf(!api.isInterface(), () -> new IllegalArgumentException("Ticket API class %s must be an interface".formatted(api.getName())));
        FunctionUtils.throwIf(impl.isInterface(), () -> new IllegalArgumentException("Ticket implementation class %s must be a concrete class".formatted(api.getName())));
        return new DefaultTicketDefinition(impl, api, prefix, Ordered.LOWEST_PRECEDENCE);
    }

    protected void registerTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        val result = customizeTicketDefinitionBeforeRegistration(ticketDefinition);
        plan.register(result);
    }

    protected TicketDefinition customizeTicketDefinitionBeforeRegistration(final TicketDefinition ticketDefinition) {
        return ticketDefinition;
    }
}
