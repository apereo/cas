package org.apereo.cas.validation;

import module java.base;
import lombok.Getter;

/**
 * This is {@link DefaultServiceTicketValidationAuthorizersExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class DefaultServiceTicketValidationAuthorizersExecutionPlan implements ServiceTicketValidationAuthorizersExecutionPlan {
    private final List<ServiceTicketValidationAuthorizer> authorizers = new ArrayList<>();

    @Override
    public void registerAuthorizer(final ServiceTicketValidationAuthorizer authorizer) {
        authorizers.add(authorizer);
    }
}
