package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of {@link MultifactorAuthenticationProviderFactoryBean} that provides instances of
 * {@link DuoSecurityMultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoSecurityMultifactorAuthenticationProviderFactory implements
    MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> {

    private final HttpClient httpClient;

    private final ChainingMultifactorAuthenticationProviderBypassEvaluator bypassEvaluator;

    private final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator;

    private final CasConfigurationProperties casProperties;

    @Override
    public DuoSecurityMultifactorAuthenticationProvider createProvider(final DuoSecurityMultifactorAuthenticationProperties properties) {
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
    protected MultifactorAuthenticationProviderBypassEvaluator getMultifactorAuthenticationProviderBypass(
        final DuoSecurityMultifactorAuthenticationProperties properties) {
        return bypassEvaluator.filterMultifactorAuthenticationProviderBypassEvaluatorsBy(properties.getId());
    }

    /**
     * Gets duo authentication service.
     *
     * @param properties the properties
     * @return the duo authentication service
     */
    protected DuoSecurityAuthenticationService getDuoAuthenticationService(final DuoSecurityMultifactorAuthenticationProperties properties) {
        if (StringUtils.isBlank(properties.getDuoApplicationKey())) {
            LOGGER.trace("Activating universal prompt authentication service for duo security");
            return new UniversalPromptDuoSecurityAuthenticationService(properties, httpClient, casProperties);
        }
        return new BasicDuoSecurityAuthenticationService(properties, httpClient);
    }
}
