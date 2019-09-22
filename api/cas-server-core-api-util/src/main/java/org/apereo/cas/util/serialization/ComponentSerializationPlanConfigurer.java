package org.apereo.cas.util.serialization;

/**
 * This is {@link ComponentSerializationPlanConfigurer}, to be implemented
 * by modules that wish to register serializable classes into the plan.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ComponentSerializationPlanConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureComponentSerializationPlan(ComponentSerializationPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
