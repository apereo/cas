package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoCredential;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
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
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final String flowId = requestContext.getActiveFlow().getId();
        final DuoMultifactorAuthenticationProvider duoProvider = (DuoMultifactorAuthenticationProvider)
                MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(flowId,
                        applicationContext).orElseThrow(AbstractMethodError::new);

        final DuoCredential c = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL, DuoCredential.class);
        c.setUsername(p.getId());
        c.setMultifactorProviderId(duoProvider.getId());

        final DuoSecurityAuthenticationService duoAuthenticationService = duoProvider.getDuoAuthenticationService();
        final MutableAttributeMap<Object> viewScope = requestContext.getViewScope();
        viewScope.put("sigRequest", duoAuthenticationService.signRequestToken(p.getId()));
        viewScope.put("apiHost", duoAuthenticationService.getApiHost());
        viewScope.put("commandName", "credential");
        viewScope.put("principal", p);
        return success();
    }
}
