package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val provider = requestContext.getFlowScope().get("provider", DuoMultifactorAuthenticationProvider.class);

        val credential = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class);
        credential.setUsername(principal.getId());
        credential.setMultifactorProviderId(provider.getId());

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val viewScope = requestContext.getViewScope();
        viewScope.put("sigRequest", duoAuthenticationService.signRequestToken(principal.getId()));
        viewScope.put("apiHost", duoAuthenticationService.getApiHost());
        viewScope.put("commandName", "credential");
        viewScope.put("principal", principal);
        return success();
    }
}
