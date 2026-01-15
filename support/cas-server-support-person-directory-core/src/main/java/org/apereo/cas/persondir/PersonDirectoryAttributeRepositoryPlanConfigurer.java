package org.apereo.cas.persondir;
import module java.base;

/**
 * This is {@link PersonDirectoryAttributeRepositoryPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface PersonDirectoryAttributeRepositoryPlanConfigurer {
    /**
     * Configure attribute repository plan.
     *
     * @param plan the plan
     */
    void configureAttributeRepositoryPlan(PersonDirectoryAttributeRepositoryPlan plan);
}
