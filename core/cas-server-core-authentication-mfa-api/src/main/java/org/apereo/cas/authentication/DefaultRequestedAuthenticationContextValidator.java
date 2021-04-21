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
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

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

    private static AuthenticationContextValidationResult toSuccessfulResult() {
        return AuthenticationContextValidationResult.builder().success(true).build();
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
        val providerResult = multifactorTriggerSelectionStrategy.resolve(request, registeredService, authentication, service);
        if (providerResult.isPresent()) {
            val provider = providerResult.get();
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
        }
        val result = authenticationContextValidator.validate(authentication,
            providerResult.map(MultifactorAuthenticationProvider::getId).orElse(StringUtils.EMPTY),
            Optional.ofNullable(registeredService));

        return AuthenticationContextValidationResult.builder()
            .success(result.isSuccess())
            .providerId(result.getProvider().map(MultifactorAuthenticationProvider::getId))
            .build();
    }
}
