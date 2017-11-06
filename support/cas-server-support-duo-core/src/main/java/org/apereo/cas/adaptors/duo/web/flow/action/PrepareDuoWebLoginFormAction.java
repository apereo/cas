package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;

/**
 * This is {@link PrepareDuoWebLoginFormAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PrepareDuoWebLoginFormAction extends AbstractAction {

    private final VariegatedMultifactorAuthenticationProvider provider;

    public PrepareDuoWebLoginFormAction(final VariegatedMultifactorAuthenticationProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();

        final DuoCredential c = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class);
        c.setUsername(p.getId());

        final Collection<MultifactorAuthenticationProvider> providers = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        providers.forEach(pr -> {
            final DuoSecurityAuthenticationService duoAuthenticationService =
                    provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class).getDuoAuthenticationService();
            requestContext.getViewScope().put("sigRequest", duoAuthenticationService.signRequestToken(p.getId()));
            requestContext.getViewScope().put("apiHost", duoAuthenticationService.getApiHost());
            requestContext.getViewScope().put("commandName", "credential");
            requestContext.getViewScope().put("principal", p);
        });
        return success();
    }
}
