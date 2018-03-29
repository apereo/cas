package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoUserAccountAuthStatus;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;

/**
 * This is {@link DetermineDuoUserAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class DetermineDuoUserAccountAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var authentication = WebUtils.getAuthentication(requestContext);
        final var p = authentication.getPrincipal();

        final var providers = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        for (final var pr : providers) {
            final var duoProvider = this.provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class);
            final var duoAuthenticationService = duoProvider.getDuoAuthenticationService();

            final var account = duoAuthenticationService.getDuoUserAccount(p.getId());
            if (account.getStatus() == DuoUserAccountAuthStatus.ENROLL && StringUtils.isNotBlank(duoProvider.getRegistrationUrl())) {
                requestContext.getFlowScope().put("duoRegistrationUrl", duoProvider.getRegistrationUrl());
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ENROLL);
            }
        }
        return success();
    }
}
