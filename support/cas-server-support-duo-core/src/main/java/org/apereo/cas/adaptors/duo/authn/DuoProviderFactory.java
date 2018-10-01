package org.apereo.cas.adaptors.duo.authn;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderFactory;
import org.apereo.cas.util.http.HttpClient;

/**
 * Implementation of {@link MultifactorAuthenticationProviderFactory} that provides instances of
 * {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoProviderFactory implements MultifactorAuthenticationProviderFactory<DuoMultifactorAuthenticationProvider,
                                                                                    DuoSecurityMultifactorProperties> {
    private final HttpClient httpClient;

    @Override
    public DuoMultifactorAuthenticationProvider create(final DuoSecurityMultifactorProperties properties) {
        final DefaultDuoMultifactorAuthenticationProvider provider = new DefaultDuoMultifactorAuthenticationProvider();
        provider.setRegistrationUrl(properties.getRegistrationUrl());
        provider.setDuoAuthenticationService(new BasicDuoSecurityAuthenticationService(properties, httpClient));
        provider.setFailureMode(properties.getFailureMode());
        provider.setBypassEvaluator(MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(properties.getBypass()));
        provider.setOrder(properties.getRank());
        provider.setId(properties.getId());
        return provider;
    }
}
