package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DefaultDuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;

import org.junit.jupiter.api.Tag;

import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDuoSecurityMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class DefaultDuoSecurityMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new DefaultDuoSecurityMultifactorAuthenticationProvider("https://www.example.org",
            mock(DuoSecurityAuthenticationService.class));
    }
}
