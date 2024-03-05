package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;

/**
 * Multifactor bypass provider that will never allow MFA to execute and will always bypass.
 *
 * @author Misagh Moayyed
 * @since 6.0
 */
@Slf4j
public class NeverAllowMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -2433888418344342672L;

    public NeverAllowMultifactorAuthenticationProviderBypassEvaluator(final ApplicationContext applicationContext) {
        super(NeverAllowMultifactorAuthenticationProviderBypassEvaluator.class.getName(), applicationContext);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        LOGGER.debug("Provider [{}] will always allow multifactor authentication to execute", provider.getId());
        return true;
    }
}
