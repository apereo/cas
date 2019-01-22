package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Implementation of {@link MultifactorAuthenticationProviderFactoryBean} that provides instances of
 * {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoSecurityMultifactorAuthenticationProviderFactory implements
    MultifactorAuthenticationProviderFactoryBean<DuoMultifactorAuthenticationProvider, DuoSecurityMultifactorProperties> {

    private final HttpClient httpClient;
    private final ChainingMultifactorAuthenticationProviderBypass bypassEvaluator;

    @Override
    public DuoMultifactorAuthenticationProvider createProvider(final DuoSecurityMultifactorProperties properties) {
        val provider = new DefaultDuoMultifactorAuthenticationProvider();
        provider.setRegistrationUrl(properties.getRegistrationUrl());
        provider.setDuoAuthenticationService(getDuoAuthenticationService(properties));
        provider.setFailureMode(properties.getFailureMode());
        provider.setBypassEvaluator(getMultifactorAuthenticationProviderBypass(properties));
        provider.setOrder(properties.getRank());
        provider.setId(properties.getId());
        return provider;
    }

    /**
     * Gets multifactor authentication provider bypass.
     *
     * @param properties the properties
     * @return the multifactor authentication provider bypass
     */
    protected MultifactorAuthenticationProviderBypass getMultifactorAuthenticationProviderBypass(final DuoSecurityMultifactorProperties properties) {
        return bypassEvaluator.filterMultifactorAuthenticationProviderBypassBy(properties.getId());
    }

    /**
     * Gets duo authentication service.
     *
     * @param properties the properties
     * @return the duo authentication service
     */
    protected DuoSecurityAuthenticationService getDuoAuthenticationService(final DuoSecurityMultifactorProperties properties) {
        return new BasicDuoSecurityAuthenticationService(properties, httpClient);
    }
}
