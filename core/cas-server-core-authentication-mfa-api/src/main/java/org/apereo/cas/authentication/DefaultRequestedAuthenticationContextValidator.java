package org.apereo.cas.authentication;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.RequestedContextValidator;

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
public class DefaultRequestedAuthenticationContextValidator implements RequestedContextValidator<MultifactorAuthenticationProvider> {
    private final ServicesManager servicesManager;
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;
    private final MultifactorAuthenticationContextValidator authenticationContextValidator;
    private final ApplicationContext applicationContext;

    @Override
    public Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validateAuthenticationContext(final Assertion assertion,
                                                                                                    final HttpServletRequest request) {
        LOGGER.debug("Locating the primary authentication associated with this service request [{}]", assertion.getService());
        val registeredService = servicesManager.findServiceBy(assertion.getService());
        val authentication = assertion.getPrimaryAuthentication();

        val requestedContext = multifactorTriggerSelectionStrategy.resolve(request, registeredService, authentication, assertion.getService());
        if (requestedContext.isEmpty()) {
            LOGGER.debug("No particular authentication context is required for this request");
            return Pair.of(Boolean.TRUE, Optional.empty());
        }

        val providerId = requestedContext.get();
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext);

        if (provider.isPresent()) {
            val bypassEvaluator = provider.get().getBypassEvaluator();
            if (!bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider.get(), request)) {
                LOGGER.debug("MFA provider [{}] was determined that it should be bypassed for this service request [{}]", providerId, assertion.getService());
                bypassEvaluator.rememberBypass(authentication, provider.get());
                return Pair.of(Boolean.TRUE, Optional.empty());
            }
        }

        return authenticationContextValidator.validate(authentication, providerId, registeredService);
    }
}
