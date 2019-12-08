package org.apereo.cas.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultServiceTicketValidationAuthorizersExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class DefaultServiceTicketValidationAuthorizersExecutionPlan implements ServiceTicketValidationAuthorizersExecutionPlan {
    private final List<ServiceTicketValidationAuthorizer> authorizers = new ArrayList<>(0);

    @Override
    public void registerAuthorizer(final ServiceTicketValidationAuthorizer authz) {
        authorizers.add(authz);
    }
}
