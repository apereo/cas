package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.authn.DuoAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();

        final DuoCredential c = requestContext.getFlowScope().get("credential", DuoCredential.class);
        c.setUsername(p.getId());

        final DuoAuthenticationService<Boolean> duoAuthenticationService =
                provider.findProvider("misagh", DuoMultifactorAuthenticationProvider.class)
                        .getDuoAuthenticationService();

        requestContext.getViewScope().put("sigRequest", duoAuthenticationService.signRequestToken(p.getId()));
        requestContext.getViewScope().put("apiHost", duoAuthenticationService.getApiHost());
        requestContext.getViewScope().put("commandName", "credential");
        requestContext.getViewScope().put("principal", p);
        return success();
    }
}
