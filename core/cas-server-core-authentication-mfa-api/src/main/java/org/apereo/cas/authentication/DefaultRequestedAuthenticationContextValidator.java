package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultRequestedAuthenticationContextValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRequestedAuthenticationContextValidator implements RequestedAuthenticationContextValidator {
    private final ServicesManager servicesManager;

    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final MultifactorAuthenticationContextValidator authenticationContextValidator;

    /**
     * To successful result.
     *
     * @return the authentication context validation result
     */
    protected static AuthenticationContextValidationResult toSuccessfulResult() {
        return AuthenticationContextValidationResult.builder().success(true).build();
    }

    /**
     * Validate multifactor provider bypass.
     *
     * @param provider          the provider
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param service           the service
     * @param request           the request
     * @return the authentication context validation result
     */
    protected static AuthenticationContextValidationResult validateMultifactorProviderBypass(final MultifactorAuthenticationProvider provider,
                                                                                             final RegisteredService registeredService,
                                                                                             final Authentication authentication,
                                                                                             final Service service,
                                                                                             final HttpServletRequest request) {
        if (provider.isAvailable(registeredService)) {
            val bypassEvaluator = provider.getBypassEvaluator();
            if (bypassEvaluator != null) {
                if (!bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request)) {
                    LOGGER.debug("MFA provider [{}] should be bypassed for this service request [{}]", provider, service);
                    bypassEvaluator.rememberBypass(authentication, provider);
                    return toSuccessfulResult();
                }
                if (bypassEvaluator.isMultifactorAuthenticationBypassed(authentication, provider.getId())) {
                    LOGGER.debug("Authentication attempt indicates that MFA is bypassed for this request for [{}]", provider);
                    bypassEvaluator.rememberBypass(authentication, provider);
                    return toSuccessfulResult();
                }
            }
        } else {
            val failure = provider.getFailureModeEvaluator().evaluate(registeredService, provider);
            if (failure != BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED) {
                return toSuccessfulResult();
            }
        }
        return toFailureResult();
    }

    private static AuthenticationContextValidationResult toFailureResult() {
        return AuthenticationContextValidationResult.builder().success(false).build();
    }

    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final Assertion assertion, final HttpServletRequest request) {
        LOGGER.trace("Locating the primary authentication associated with this service request [{}]", assertion.getService());
        val registeredService = servicesManager.findServiceBy(assertion.getService());
        val authentication = assertion.getPrimaryAuthentication();
        return validateAuthenticationContext(request, registeredService, authentication, assertion.getService());
    }

    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final HttpServletRequest request,
                                                                               final RegisteredService registeredService,
                                                                               final Authentication authentication,
                                                                               final Service service) {
        if (registeredService != null && registeredService.getMultifactorPolicy().isBypassEnabled()) {
            LOGGER.debug("Multifactor authentication execution is ignored for [{}]", registeredService.getName());
            return toSuccessfulResult();
        }

        val providerResult = multifactorTriggerSelectionStrategy.resolve(request, registeredService, authentication, service);
        if (providerResult.isEmpty()) {
            LOGGER.debug("No authentication context is required for this request");
            return toSuccessfulResult();
        }

        val providers = providerResult
            .map(provider -> {
                if (provider instanceof ChainingMultifactorAuthenticationProvider) {
                    val chain = ChainingMultifactorAuthenticationProvider.class.cast(provider);
                    return chain.getMultifactorAuthenticationProviders().stream()
                        .filter(p -> p.equals(provider)).collect(Collectors.toList());
                }
                return List.of(provider);
            })
            .orElse(List.of());

        if (providers.stream()
            .map(provider -> validateMultifactorProviderBypass(provider, registeredService, authentication, service, request))
            .allMatch(AuthenticationContextValidationResult::isSuccess)) {
            return toSuccessfulResult();
        }

        LOGGER.debug("Multifactor providers eligible for validation are [{}]", providers);
        return providers.stream()
            .sorted(Comparator.comparing(MultifactorAuthenticationProvider::getOrder))
            .map(provider -> authenticationContextValidator.validate(authentication,
                provider.getId(),
                Optional.ofNullable(registeredService)))
            .filter(MultifactorAuthenticationContextValidationResult::isSuccess)
            .findAny()
            .map(result -> AuthenticationContextValidationResult.builder()
                .success(result.isSuccess())
                .contextId(result.getProvider().map(MultifactorAuthenticationProvider::getId))
                .build())
            .map(AuthenticationContextValidationResult.class::cast)
            .orElseGet(DefaultRequestedAuthenticationContextValidator::toFailureResult);
    }
}
