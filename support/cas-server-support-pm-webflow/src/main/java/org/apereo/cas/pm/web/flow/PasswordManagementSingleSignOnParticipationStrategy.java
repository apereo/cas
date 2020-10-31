package org.apereo.cas.pm.web.flow;

import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategy}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class PasswordManagementSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        return false;
    }
}
