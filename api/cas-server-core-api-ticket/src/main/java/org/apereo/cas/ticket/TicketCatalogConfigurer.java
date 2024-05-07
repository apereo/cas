package org.apereo.cas.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * This is {@link TicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface TicketCatalogConfigurer extends Ordered {
    /**
     * configure the plan.
     *
     * @param plan          the plan
     * @param casProperties the cas properties
     * @throws Throwable the throwable
     */
    void configureTicketCatalog(TicketCatalog plan, CasConfigurationProperties casProperties) throws Throwable;

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
