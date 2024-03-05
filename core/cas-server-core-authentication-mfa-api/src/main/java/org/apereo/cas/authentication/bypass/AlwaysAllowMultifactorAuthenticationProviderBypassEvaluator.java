package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;

/**
 * Multifactor Bypass provider that will never allow MFA to execute and will always bypass.
 *
 * @author Misagh Moayyed
 * @since 7.0
 */
@Slf4j
public class AlwaysAllowMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -1433888418344342672L;

    public AlwaysAllowMultifactorAuthenticationProviderBypassEvaluator(final ApplicationContext applicationContext) {
        super(AlwaysAllowMultifactorAuthenticationProviderBypassEvaluator.class.getSimpleName(), applicationContext);
    }


    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        LOGGER.debug("Provider [{}] will not allow multifactor authentication to execute", provider.getId());
        return false;
    }
}
