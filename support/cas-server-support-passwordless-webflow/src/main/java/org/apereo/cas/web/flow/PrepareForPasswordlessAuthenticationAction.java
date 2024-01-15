package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PrepareForPasswordlessAuthenticationAction extends BasePasswordlessCasWebflowAction {

    public PrepareForPasswordlessAuthenticationAction(final CasConfigurationProperties casProperties) {
        super(casProperties);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val pwdresetTicket = requestContext.getRequestParameters().get(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN);
        if (StringUtils.isNotBlank(pwdresetTicket)) {
            return success();
        }

        PasswordlessWebflowUtils.putPasswordlessAuthenticationEnabled(requestContext, Boolean.TRUE);
        if (!PasswordlessWebflowUtils.hasPasswordlessAuthenticationAccount(requestContext) && isLoginFlowActive(requestContext)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID);
        }
        return null;
    }
}
