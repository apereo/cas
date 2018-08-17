package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
@RequiredArgsConstructor
public class PrepareDuoWebLoginFormAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;
    private final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val p = WebUtils.getAuthentication(requestContext).getPrincipal();

        val c = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class);
        c.setUsername(p.getId());

        val providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        val providers =
            MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds, applicationContext);

        providers.forEach(pr -> {
            val duoAuthenticationService =
                provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class).getDuoAuthenticationService();
            val viewScope = requestContext.getViewScope();
            viewScope.put("sigRequest", duoAuthenticationService.signRequestToken(p.getId()));
            viewScope.put("apiHost", duoAuthenticationService.getApiHost());
            viewScope.put("commandName", "credential");
            viewScope.put("principal", p);
        });
        return success();
    }
}
