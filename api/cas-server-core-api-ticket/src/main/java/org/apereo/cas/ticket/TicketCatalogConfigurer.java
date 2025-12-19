package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

/**
 * This is {@link TicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface TicketCatalogConfigurer extends Ordered, NamedObject {
    /**
     * configure the plan.
     *
     * @param plan          the plan
     * @param casProperties the cas properties
     * @throws Throwable the throwable
     */
    void configureTicketCatalog(TicketCatalog plan, CasConfigurationProperties casProperties) throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
