package org.apereo.cas.validation;

/**
 * This is {@link ServiceTicketValidationAuthorizerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ServiceTicketValidationAuthorizerConfigurer {

    /**
     * Configure authorizer execution plan.
     *
     * @param plan the plan
     */
    void configureAuthorizersExecutionPlan(ServiceTicketValidationAuthorizersExecutionPlan plan);
    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
