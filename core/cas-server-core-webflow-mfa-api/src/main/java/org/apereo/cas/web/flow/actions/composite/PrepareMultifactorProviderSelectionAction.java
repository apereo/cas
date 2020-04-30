package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.stream.Collectors;

/**
 * This is {@link PrepareMultifactorProviderSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class PrepareMultifactorProviderSelectionAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();

        val registeredService = (RegisteredService) attributes.get(RegisteredService.class.getName());
        WebUtils.putRegisteredService(requestContext, registeredService);

        val mfaProvider = (ChainingMultifactorAuthenticationProvider)
            attributes.get(MultifactorAuthenticationProvider.class.getName());

        val authn = WebUtils.getAuthentication(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        val mfaProviders = mfaProvider.getMultifactorAuthenticationProviders()
            .stream()
            .filter(p -> p.isAvailable(registeredService)
                && p.getBypassEvaluator().shouldMultifactorAuthenticationProviderExecute(
                authn, registeredService, p, request))
            .map(MultifactorAuthenticationProvider::getId)
            .collect(Collectors.toList());

        WebUtils.putSelectableMultifactorAuthenticationProviders(requestContext, mfaProviders);
        return null;
    }
}
