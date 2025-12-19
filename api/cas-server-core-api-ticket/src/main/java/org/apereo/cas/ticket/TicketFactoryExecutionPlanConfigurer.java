package org.apereo.cas.ticket;
import module java.base;

/**
 * This is {@link TicketFactoryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface TicketFactoryExecutionPlanConfigurer {

    /**
     * Configure ticket factory.
     *
     * @return the ticket factory
     */
    TicketFactory configureTicketFactory();
}
