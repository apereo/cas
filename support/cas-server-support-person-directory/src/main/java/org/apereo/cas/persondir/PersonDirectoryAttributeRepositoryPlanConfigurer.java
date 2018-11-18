package org.apereo.cas.persondir;

/**
 * This is {@link PersonDirectoryAttributeRepositoryPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface PersonDirectoryAttributeRepositoryPlanConfigurer {
    /**
     * Configure attribute repository plan.
     *
     * @param plan the plan
     */
    default void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
    }
}
