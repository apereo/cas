package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Default implementation of {@link MultifactorAuthenticationFailureModeEvaluator}.
 *
 * @author Travis Schmidt
 * @since 6.0.5
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationFailureModeEvaluator implements MultifactorAuthenticationFailureModeEvaluator {

    private final CasConfigurationProperties casProperties;

    @Override
    public RegisteredServiceMultifactorPolicy.FailureModes evaluate(final RegisteredService service,
                                                                    final MultifactorAuthenticationProvider provider) {
        var failureMode = RegisteredServiceMultifactorPolicy.FailureModes.valueOf(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        LOGGER.debug("Setting failure mode to [{}] based on Global Policy", failureMode);

        if (provider.failureMode() != RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED) {
            LOGGER.debug("Provider failure mode [{}] overriding Global mode [{}]", provider.failureMode(), failureMode);
            failureMode = provider.failureMode();
        }

        if (service != null) {
            val policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() != RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED) {
                LOGGER.debug("Service failure mode [{}] overriding current failure mode [{}]", policy.getFailureMode(), failureMode);
                failureMode = policy.getFailureMode();
            }
        }
        return failureMode;
    }
}
