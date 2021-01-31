package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.RegisteredService;

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

    private static final long serialVersionUID = 3837589092620951038L;

    private final CasConfigurationProperties casProperties;

    @Override
    public BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes evaluate(final RegisteredService service,
                                                                  final MultifactorAuthenticationProvider provider) {
        var failureMode = casProperties.getAuthn().getMfa().getCore().getGlobalFailureMode();
        LOGGER.debug("Setting failure mode to [{}] based on Global Policy", failureMode);

        if (provider.getFailureMode() != BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED) {
            LOGGER.debug("Provider failure mode [{}] overriding Global mode [{}]", provider.getFailureMode(), failureMode);
            failureMode = provider.getFailureMode();
        }

        if (service != null) {
            val policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() != BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED) {
                LOGGER.debug("Service failure mode [{}] overriding current failure mode [{}]", policy.getFailureMode(), failureMode);
                failureMode = policy.getFailureMode();
            }
        }
        return failureMode;
    }
}
