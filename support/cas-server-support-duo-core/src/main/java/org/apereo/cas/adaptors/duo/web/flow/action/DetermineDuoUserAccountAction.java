package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.mfa.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DetermineDuoUserAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DetermineDuoUserAccountAction extends AbstractMultifactorAuthenticationAction<DuoMultifactorAuthenticationProvider> {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Principal p = authentication.getPrincipal();

        final DuoSecurityAuthenticationService duoAuthenticationService = provider.getDuoAuthenticationService();
        final DuoUserAccount account = duoAuthenticationService.getDuoUserAccount(p.getId());
        if (account.getStatus() == DuoUserAccountAuthStatus.ENROLL) {
            if (!StringUtils.isEmpty(provider.getRegistrationUrl())) {
                LOGGER.error("Duo webflow resolved to event ENROLL, but no registration url was provided.");
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
            }
            requestContext.getFlowScope().put("duoRegistrationUrl", provider.getRegistrationUrl());
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
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
