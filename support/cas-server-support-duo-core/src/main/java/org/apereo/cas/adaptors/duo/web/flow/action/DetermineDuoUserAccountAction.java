package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
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
public class DetermineDuoUserAccountAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val service = WebUtils.getRegisteredService(requestContext);
        val provider = requestContext.getFlowScope().get("provider",
                DuoMultifactorAuthenticationProvider.class);

        val enrollEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
        val denyEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        val unavailableEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        val errorEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val account = duoAuthenticationService.getDuoUserAccount(principal.getId());
        switch (account.getStatus()) {
            case ENROLL:
                if (StringUtils.isEmpty(provider.getRegistrationUrl())) {
                    LOGGER.error("Duo webflow resolved to event ENROLL, but no registration url was provided.");
                    return errorEvent;
                }
                requestContext.getFlowScope().put("duoRegistrationUrl", provider.getRegistrationUrl());
                return enrollEvent;
            case ALLOW:
                return updateAuthenticationForMultifactorBypass(authentication, provider.getId());
            case DENY:
                return denyEvent;
            case UNAVAILABLE:
                val failureMode = provider.determineFailureMode(service);
                if (failureMode != RegisteredServiceMultifactorPolicy.FailureModes.CLOSED) {
                    return updateAuthenticationForMultifactorBypass(authentication, provider.getId());
                }
                return unavailableEvent;
            default:
        }
        return success();
    }

    private Event updateAuthenticationForMultifactorBypass(final Authentication authentication, final String providerId) {
        authentication.addAttribute(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, providerId);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
    }
}
