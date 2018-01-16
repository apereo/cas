package org.apereo.cas.validation;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultServiceTicketValidationAuthorizersExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultServiceTicketValidationAuthorizersExecutionPlan implements ServiceTicketValidationAuthorizersExecutionPlan {


    private final List<ServiceTicketValidationAuthorizer> serviceTicketValidationAuthorizers = new ArrayList<>();


    @Override
    public void registerAuthorizer(final ServiceTicketValidationAuthorizer authz) {
        serviceTicketValidationAuthorizers.add(authz);
    }

    @Override
    public Collection<ServiceTicketValidationAuthorizer> getAuthorizers() {
        return this.serviceTicketValidationAuthorizers;
    }
}
