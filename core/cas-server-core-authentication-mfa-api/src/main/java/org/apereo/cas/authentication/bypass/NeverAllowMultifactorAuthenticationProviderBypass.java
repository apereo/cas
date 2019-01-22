package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass provider based on Credentials.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class NeverAllowMultifactorAuthenticationProviderBypass extends BaseMultifactorAuthenticationProviderBypass {
    private static final long serialVersionUID = -2433888418344342672L;
    private static volatile MultifactorAuthenticationProviderBypass INSTANCE;

    protected NeverAllowMultifactorAuthenticationProviderBypass() {
        super(NeverAllowMultifactorAuthenticationProviderBypass.class.getSimpleName());
    }


    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static MultifactorAuthenticationProviderBypass getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NeverAllowMultifactorAuthenticationProviderBypass();
        }
        return INSTANCE;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        return true;
    }
}
