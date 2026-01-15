package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link LogoutExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface LogoutExecutionPlanConfigurer extends NamedObject {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureLogoutExecutionPlan(LogoutExecutionPlan plan);
}
