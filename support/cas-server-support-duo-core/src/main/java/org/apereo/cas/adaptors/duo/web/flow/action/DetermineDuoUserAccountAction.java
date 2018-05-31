package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
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
@Slf4j
@RequiredArgsConstructor
public class DetermineDuoUserAccountAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;
    private final ApplicationContext applicationContext;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        final var authentication = WebUtils.getAuthentication(requestContext);
        final var principal = authentication.getPrincipal();

        final var enrollEvent = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
        final var providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        final var providers = MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds, applicationContext);

        for (final var pr : providers) {
            final var duoProvider = this.provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class);
            final var duoAuthenticationService = duoProvider.getDuoAuthenticationService();
            final var account = duoAuthenticationService.getDuoUserAccount(principal.getId());
            if (account.getStatus() == DuoUserAccountAuthStatus.ENROLL && StringUtils.isNotBlank(duoProvider.getRegistrationUrl())) {
                requestContext.getFlowScope().put("duoRegistrationUrl", duoProvider.getRegistrationUrl());
                return enrollEvent;
            }
        }
        return success();
    }
}
