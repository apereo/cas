package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link PrepareMultifactorProviderSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class PrepareMultifactorProviderSelectionAction extends BaseCasWebflowAction {
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();

        val registeredService = (RegisteredService) attributes.get(RegisteredService.class.getName());
        val service = (Service) attributes.get(Service.class.getName());
        WebUtils.putRegisteredService(requestContext, registeredService);

        val mfaProvider = (ChainingMultifactorAuthenticationProvider)
            attributes.get(MultifactorAuthenticationProvider.class.getName());

        val authn = WebUtils.getAuthentication(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        val mfaProviders = mfaProvider.getMultifactorAuthenticationProviders()
            .stream()
            .filter(provider -> provider.isAvailable(registeredService)
                && provider.getBypassEvaluator().shouldMultifactorAuthenticationProviderExecute(authn, registeredService, provider, request, service))
            .sorted(Comparator.comparing(MultifactorAuthenticationProvider::getOrder))
            .map(MultifactorAuthenticationProvider::getId)
            .collect(Collectors.toList());

        MultifactorAuthenticationWebflowUtils.putSelectableMultifactorAuthenticationProviders(requestContext, mfaProviders);
        return null;
    }
}
