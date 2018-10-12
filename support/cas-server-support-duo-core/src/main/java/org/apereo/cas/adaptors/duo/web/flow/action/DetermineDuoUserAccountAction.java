package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DetermineDuoUserAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DetermineDuoUserAccountAction extends AbstractMultifactorAuthenticationAction<DuoMultifactorAuthenticationProvider> {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val account = duoAuthenticationService.getDuoUserAccount(principal.getId());

        if (account.getStatus() == DuoUserAccountAuthStatus.ENROLL) {
            if (StringUtils.isNotBlank(provider.getRegistrationUrl())) {
                requestContext.getFlowScope().put("duoRegistrationUrl", provider.getRegistrationUrl());
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
            }
        }
        if (account.getStatus() == DuoUserAccountAuthStatus.ALLOW) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
        if (account.getStatus() == DuoUserAccountAuthStatus.DENY) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }
        if (account.getStatus() == DuoUserAccountAuthStatus.UNAVAILABLE) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        }

        return success();
    }

}
