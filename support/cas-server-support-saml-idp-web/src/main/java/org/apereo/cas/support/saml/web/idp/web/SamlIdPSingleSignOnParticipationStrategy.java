package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link SamlIdPSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class SamlIdPSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    private final MultifactorAuthenticationContextValidator authenticationContextValidator;

    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    public SamlIdPSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                                    final MultifactorAuthenticationContextValidator authenticationContextValidator,
                                                    final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.authenticationContextValidator = authenticationContextValidator;
        this.multifactorTriggerSelectionStrategy = multifactorTriggerSelectionStrategy;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) throws Throwable {
        val service = ssoRequest.getAttributeValue(Service.class.getName(), Service.class);
        val registeredService = ssoRequest.getAttributeValue(RegisteredService.class.getName(), RegisteredService.class);
        val authentication = ssoRequest.getAttributeValue(Authentication.class.getName(), Authentication.class);
        val request = ssoRequest.getHttpServletRequest()
            .orElseGet(() -> WebUtils.getHttpServletRequestFromExternalWebflowContext(ssoRequest.getRequestContext().orElseThrow()));
        val response = ssoRequest.getHttpServletResponse()
            .orElseGet(() -> WebUtils.getHttpServletResponseFromExternalWebflowContext(ssoRequest.getRequestContext().orElseThrow()));

        val authnRequest = ssoRequest.getAttributeValue(AuthnRequest.class.getName(), AuthnRequest.class);
        val initialResult = supports(ssoRequest) && Boolean.FALSE.equals(authnRequest.isForceAuthn());

        return FunctionUtils.doAndHandle(
            () -> resolveMultifactorAuthenticationTrigger(service, registeredService, authentication, request, response)
                .map(requestedContext -> {
                    LOGGER.trace("Validating authentication context for event [{}] and service [{}]", requestedContext.getId(), registeredService);
                    val result = authenticationContextValidator.validate(authentication,
                        requestedContext.getId(), Optional.ofNullable(registeredService));
                    val validatedProvider = result.getProvider();
                    return initialResult && result.isSuccess() && validatedProvider.isPresent()
                           && !registeredService.getMultifactorAuthenticationPolicy().isForceExecution();
                })
            .orElse(initialResult), throwable -> false).get();
    }

    protected Optional<MultifactorAuthenticationProvider> resolveMultifactorAuthenticationTrigger(
        final Service service, final RegisteredService registeredService,
        final Authentication authentication, final HttpServletRequest request,
        final HttpServletResponse response) throws Throwable {
        return multifactorTriggerSelectionStrategy.resolve(request, response,
            registeredService, authentication, service);
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.containsAttribute(AuthnRequest.class.getName())
               && ssoRequest.containsAttribute(Issuer.class.getName());
    }
}
