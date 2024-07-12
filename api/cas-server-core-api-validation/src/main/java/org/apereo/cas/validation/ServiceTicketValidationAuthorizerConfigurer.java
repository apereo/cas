package org.apereo.cas.validation;

import org.apereo.cas.util.NamedObject;

/**
 * This is {@link ServiceTicketValidationAuthorizerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ServiceTicketValidationAuthorizerConfigurer extends NamedObject {

    /**
     * Configure authorizer execution plan.
     *
     * @param plan the plan
     */
    void configureAuthorizersExecutionPlan(ServiceTicketValidationAuthorizersExecutionPlan plan);
}
