package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of {@link MultifactorAuthenticationProviderFactoryBean} that provides instances of
 * {@link DuoSecurityMultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
public class DuoSecurityMultifactorAuthenticationProviderFactory implements
    MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorProperties> {

    private final HttpClient httpClient;
    private final ChainingMultifactorAuthenticationProviderBypassEvaluator bypassEvaluator;
    private final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    @Override
    public DuoSecurityMultifactorAuthenticationProvider createProvider(final DuoSecurityMultifactorProperties properties) {
        val provider = new DefaultDuoSecurityMultifactorAuthenticationProvider();
        provider.setRegistrationUrl(properties.getRegistrationUrl());
        provider.setDuoAuthenticationService(getDuoAuthenticationService(properties));
        provider.setFailureMode(properties.getFailureMode());
        provider.setFailureModeEvaluator(failureModeEvaluator);
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
    protected MultifactorAuthenticationProviderBypassEvaluator getMultifactorAuthenticationProviderBypass(final DuoSecurityMultifactorProperties properties) {
        return bypassEvaluator.filterMultifactorAuthenticationProviderBypassEvaluatorsBy(properties.getId());
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
