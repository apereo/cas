package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import javax.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass provider based on Credentials.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public class NeverAllowMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = -2433888418344342672L;

    private static MultifactorAuthenticationProviderBypassEvaluator INSTANCE;

    protected NeverAllowMultifactorAuthenticationProviderBypassEvaluator() {
        super(NeverAllowMultifactorAuthenticationProviderBypassEvaluator.class.getSimpleName());
    }


    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static MultifactorAuthenticationProviderBypassEvaluator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NeverAllowMultifactorAuthenticationProviderBypassEvaluator();
        }
        return INSTANCE;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        return true;
    }
}
