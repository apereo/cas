package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass Provider based on Service Multifactor Policy.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class RegisteredServiceMultifactorAuthenticationProviderBypass extends BaseMultifactorAuthenticationProviderBypass {
    private static final long serialVersionUID = -3553888418344342672L;

    public RegisteredServiceMultifactorAuthenticationProviderBypass(final String providerId) {
        super(providerId);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {

        return registeredService == null
            || registeredService.getMultifactorPolicy() == null
            || !registeredService.getMultifactorPolicy().isBypassEnabled();
    }
}
