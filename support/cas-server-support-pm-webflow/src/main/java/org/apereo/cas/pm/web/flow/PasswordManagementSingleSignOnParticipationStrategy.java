package org.apereo.cas.pm.web.flow;

import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategy}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class PasswordManagementSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {

    @Override
    public boolean supports(final RequestContext requestContext) {
        val params = requestContext.getRequestParameters();
        return params.contains(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
    }

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        return false;
    }
}
