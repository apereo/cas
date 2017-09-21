package org.apereo.cas;

/**
 * This is {@link ComponentSerializationPlanConfigurator}, to be implemented
 * by modules that wish to register serializable classes into the plan.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface ComponentSerializationPlanConfigurator {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {}
}
