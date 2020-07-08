package org.apereo.cas.ticket;

/**
 * This is {@link TicketFactoryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface TicketFactoryExecutionPlanConfigurer {

    TicketFactory configureTicketFactory();
}
