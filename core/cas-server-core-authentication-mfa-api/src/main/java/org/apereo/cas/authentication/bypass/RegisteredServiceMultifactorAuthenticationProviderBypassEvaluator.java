package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import org.springframework.context.ConfigurableApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;

/**
 * Multifactor Bypass Provider based on Service Multifactor Policy.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public class RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -3553888418344342672L;

    public RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(final String providerId, final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        return registeredService == null
               || registeredService.getMultifactorAuthenticationPolicy() == null
               || !registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled();
    }
}
