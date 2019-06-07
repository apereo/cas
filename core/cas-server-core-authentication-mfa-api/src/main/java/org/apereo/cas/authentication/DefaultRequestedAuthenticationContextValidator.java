package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;

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
public class DefaultRequestedAuthenticationContextValidator implements RequestedAuthenticationContextValidator<MultifactorAuthenticationProvider> {
    private final ServicesManager servicesManager;
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;
    private final MultifactorAuthenticationContextValidator authenticationContextValidator;
    private final ApplicationContext applicationContext;

    @Override
    public Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validateAuthenticationContext(final Assertion assertion,
                                                                                                    final HttpServletRequest request) {
        LOGGER.trace("Locating the primary authentication associated with this service request [{}]", assertion.getService());
        val registeredService = servicesManager.findServiceBy(assertion.getService());
        val authentication = assertion.getPrimaryAuthentication();

        val requestedContext = multifactorTriggerSelectionStrategy.resolve(request, registeredService,
            authentication, assertion.getService());
        if (requestedContext.isEmpty()) {
            LOGGER.debug("No particular authentication context is required for this request");
            return Pair.of(Boolean.TRUE, Optional.empty());
        }

        val providerId = requestedContext.get();
        val providerResult = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext);

        if (providerResult.isPresent()) {
            val provider = providerResult.get();
            if (provider.isAvailable(registeredService)) {
                val bypassEvaluator = provider.getBypassEvaluator();
                if (!bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request)) {
                    LOGGER.debug("MFA provider [{}] has determined that it should be bypassed for this service request [{}]",
                            providerId, assertion.getService());
                    bypassEvaluator.rememberBypass(authentication, provider);
                    return Pair.of(Boolean.TRUE, Optional.empty());
                }
                /**
                 * Not Sure this should be here.  Could result in MFA bypass in one service bypasses all other sso services.
                 */
                if (bypassEvaluator.isMultifactorAuthenticationBypassed(authentication, providerId)) {
                    LOGGER.debug("Authentication attempt indicates that MFA is bypassed for this request for [{}]", requestedContext);
                    bypassEvaluator.rememberBypass(authentication, provider);
                    return Pair.of(Boolean.TRUE, Optional.empty());
                }
            } else {
                val failure = provider.getFailureModeEvaluator().evaluate(registeredService, provider);
                if (failure != RegisteredServiceMultifactorPolicyFailureModes.CLOSED) {
                    return Pair.of(Boolean.TRUE, Optional.empty());
                }
            }
        }

        return authenticationContextValidator.validate(authentication, providerId, registeredService);
    }
}
