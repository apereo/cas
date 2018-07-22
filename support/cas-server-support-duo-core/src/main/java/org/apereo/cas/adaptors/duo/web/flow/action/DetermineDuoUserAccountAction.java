package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
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
public class DetermineDuoUserAccountAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;
    private final ApplicationContext applicationContext;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val enrollEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
        val providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        val providers = MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds, applicationContext);

        for (val pr : providers) {
            val duoProvider = this.provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class);
            val duoAuthenticationService = duoProvider.getDuoAuthenticationService();
            val account = duoAuthenticationService.getDuoUserAccount(principal.getId());
            if (account.getStatus() == DuoUserAccountAuthStatus.ENROLL && StringUtils.isNotBlank(duoProvider.getRegistrationUrl())) {
                requestContext.getFlowScope().put("duoRegistrationUrl", duoProvider.getRegistrationUrl());
                return enrollEvent;
            }
        }
        return success();
    }
}
