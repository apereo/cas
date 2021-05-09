package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;

/**
 * This is {@link DuoSecurityPrepareWebLoginFormAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DuoSecurityPrepareWebLoginFormAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal());
        val credential = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoSecurityCredential.class);
        Objects.requireNonNull(credential).setUsername(principal.getId());
        credential.setProviderId(provider.getId());

        val duoAuthenticationService = provider.getDuoAuthenticationService();
        val viewScope = requestContext.getViewScope();
        duoAuthenticationService.signRequestToken(principal.getId())
            .ifPresent(value -> viewScope.put("sigRequest", value));
        viewScope.put("apiHost", duoAuthenticationService.getApiHost());
        viewScope.put("principal", principal);
        return success();
    }
}
