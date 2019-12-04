package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoSecurityDetermineUserAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DuoSecurityDetermineUserAccountAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {

    public DuoSecurityDetermineUserAccountAction(final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val account = duoAuthenticationService.getUserAccount(principal.getId());

        val eventFactorySupport = new EventFactorySupport();
        if (account.getStatus() == DuoSecurityUserAccountStatus.ENROLL) {
            if (StringUtils.isNotBlank(provider.getRegistrationUrl())) {
                requestContext.getFlowScope().put("duoRegistrationUrl", provider.getRegistrationUrl());
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
            }
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.ALLOW) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.DENY) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }
        if (account.getStatus() == DuoSecurityUserAccountStatus.UNAVAILABLE) {
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        }

        return success();
    }

}
