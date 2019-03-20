package org.apereo.cas.ticket.serialization;

/**
 * This is {@link TicketSerializationExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface TicketSerializationExecutionPlanConfigurer {
    /**
     * Configure ticket serialization.
     *
     * @param plan the plan
     */
    void configureTicketSerialization(TicketSerializationExecutionPlan plan);
}
