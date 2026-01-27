package org.apereo.cas.web.flow.actions.composite;

import module java.base;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareMultifactorProviderSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class PrepareMultifactorProviderSelectionAction extends BaseCasWebflowAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();
        val registeredService = attributes.get(RegisteredService.class.getName(), RegisteredService.class);
        val service = attributes.get(Service.class.getName(), Service.class);
        WebUtils.putRegisteredService(requestContext, registeredService);
        prepareSelectableMultifactorProviders(requestContext, registeredService, service);
        determineMultifactorProviderOptional(requestContext, registeredService, service);
        return null;
    }

    protected void determineMultifactorProviderOptional(final RequestContext requestContext,
                                                        final RegisteredService registeredService,
                                                        final Service service) {
        val providerSelection = casProperties.getAuthn().getMfa().getCore().getProviderSelection();
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationOptional(requestContext,
            providerSelection.isProviderSelectionEnabled()
                && providerSelection.isProviderSelectionOptional()
                && !requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET));
    }

    protected void prepareSelectableMultifactorProviders(final RequestContext requestContext,
                                                         final RegisteredService registeredService,
                                                         final Service service) {
        val mfaProvider = (ChainingMultifactorAuthenticationProvider)
            requestContext.getCurrentEvent().getAttributes().get(MultifactorAuthenticationProvider.class.getName());
        val authentication = WebUtils.getAuthentication(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val mfaProviders = mfaProvider.getMultifactorAuthenticationProviders()
            .stream()
            .filter(provider -> provider.isAvailable(registeredService)
                && provider.getBypassEvaluator().shouldMultifactorAuthenticationProviderExecute(
                    authentication, registeredService, provider, request, service))
            .sorted(Comparator.comparing(MultifactorAuthenticationProvider::getOrder))
            .map(MultifactorAuthenticationProvider::getId)
            .collect(Collectors.toList());
        MultifactorAuthenticationWebflowUtils.putSelectableMultifactorAuthenticationProviders(requestContext, mfaProviders);
    }
}
