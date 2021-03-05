package org.apereo.cas.authentication;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationFailureModeEvaluator}.
 *
 * @author Travis Schmidt
 * @since 6.0.5
 */
@FunctionalInterface
public interface MultifactorAuthenticationFailureModeEvaluator extends Serializable, Ordered {


    /**
     * Eval current failureMode rules for the provider.
     *
     * @param registeredService the registered service in question
     * @param provider          the provider
     * @return failure mode.
     */
    BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes evaluate(RegisteredService registeredService,
                                                                                                           MultifactorAuthenticationProvider provider);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
