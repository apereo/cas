package org.apereo.cas.validation;

import module java.base;

/**
 * This is {@link ServiceTicketValidationAuthorizersExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface ServiceTicketValidationAuthorizersExecutionPlan {

    /**
     * Register authorizer.
     *
     * @param authorizer the authz
     */
    void registerAuthorizer(ServiceTicketValidationAuthorizer authorizer);

    /**
     * Gets authorizers.
     *
     * @return the authorizers
     */
    Collection<ServiceTicketValidationAuthorizer> getAuthorizers();
}
