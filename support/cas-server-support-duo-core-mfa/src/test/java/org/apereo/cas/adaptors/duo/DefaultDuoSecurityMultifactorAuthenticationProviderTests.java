package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DefaultDuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationRegistrationProperties;

import org.junit.jupiter.api.Tag;

import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDuoSecurityMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("DuoSecurity")
class DefaultDuoSecurityMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new DefaultDuoSecurityMultifactorAuthenticationProvider(
            new DuoSecurityMultifactorAuthenticationRegistrationProperties()
                .setRegistrationUrl("https://www.example.org"),
            mock(DuoSecurityAuthenticationService.class));
    }
}
