package org.apereo.cas.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultServiceTicketValidationAuthorizersExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultServiceTicketValidationAuthorizersExecutionPlan implements ServiceTicketValidationAuthorizersExecutionPlan {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceTicketValidationAuthorizersExecutionPlan.class);

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
