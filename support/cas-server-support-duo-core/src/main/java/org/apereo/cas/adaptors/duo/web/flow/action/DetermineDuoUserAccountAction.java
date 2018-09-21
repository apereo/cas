package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
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
@Slf4j
@RequiredArgsConstructor
public class DetermineDuoUserAccountAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Principal p = authentication.getPrincipal();
        final String flowId = requestContext.getActiveFlow().getId();
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final DuoMultifactorAuthenticationProvider provider = (DuoMultifactorAuthenticationProvider)
                MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(CollectionUtils.wrap(flowId),
                        applicationContext).iterator().next();

        final Event enrollEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
        final Event denyEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        final Event unavailableEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        final Event errorEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);

        final DuoSecurityAuthenticationService duoAuthenticationService = provider.getDuoAuthenticationService();
        final DuoUserAccount account = duoAuthenticationService.getDuoUserAccount(p.getId());
        switch (account.getStatus()) {
            case ENROLL:
                if (!StringUtils.hasText(provider.getRegistrationUrl())) {
                    LOGGER.error("Duo webflow resolved to event ENROLL, but no registration url was provided.");
                    return errorEvent;
                }
                requestContext.getFlowScope().put("duoRegistrationUrl", provider.getRegistrationUrl());
                return enrollEvent;
            case ALLOW:
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
            case DENY:
                return denyEvent;
            case UNAVAILABLE:
                return unavailableEvent;
            default:
        }
        return success();
    }

}
