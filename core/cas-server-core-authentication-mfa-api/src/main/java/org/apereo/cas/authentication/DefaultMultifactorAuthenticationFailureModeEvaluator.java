package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

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
    public RegisteredServiceMultifactorPolicyFailureModes evaluate(final RegisteredService service,
                                                                   final MultifactorAuthenticationProvider provider) {
        var failureMode = RegisteredServiceMultifactorPolicyFailureModes.valueOf(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        LOGGER.debug("Setting failure mode to [{}] based on Global Policy", failureMode);

        if (provider.getFailureMode() != RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED) {
            LOGGER.debug("Provider failure mode [{}] overriding Global mode [{}]", provider.getFailureMode(), failureMode);
            failureMode = provider.getFailureMode();
        }

        if (service != null) {
            val policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() != RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED) {
                LOGGER.debug("Service failure mode [{}] overriding current failure mode [{}]", policy.getFailureMode(), failureMode);
                failureMode = policy.getFailureMode();
            }
        }
        return failureMode;
    }
}
