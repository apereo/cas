package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareDuoWebLoginFormAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class PrepareDuoWebLoginFormAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;
    private final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var p = WebUtils.getAuthentication(requestContext).getPrincipal();

        final var c = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class);
        c.setUsername(p.getId());

        final var providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        final var providers =
            MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds, applicationContext);

        providers.forEach(pr -> {
            final var duoAuthenticationService =
                provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class).getDuoAuthenticationService();
            final var viewScope = requestContext.getViewScope();
            viewScope.put("sigRequest", duoAuthenticationService.signRequestToken(p.getId()));
            viewScope.put("apiHost", duoAuthenticationService.getApiHost());
            viewScope.put("commandName", "credential");
            viewScope.put("principal", p);
        });
        return success();
    }
}
