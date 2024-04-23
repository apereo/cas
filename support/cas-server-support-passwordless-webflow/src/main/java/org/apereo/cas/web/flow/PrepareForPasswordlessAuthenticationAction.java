package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
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
        PasswordlessWebflowUtils.putPasswordlessAuthenticationEnabled(requestContext, Boolean.TRUE);

        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getPasswordlessPolicy() != null && !registeredService.getPasswordlessPolicy().isEnabled()) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_SKIP);
        }
        
        if (!PasswordlessWebflowUtils.hasPasswordlessAuthenticationAccount(requestContext) && isLoginFlowActive(requestContext)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID);
        }
        return null;
    }
}
