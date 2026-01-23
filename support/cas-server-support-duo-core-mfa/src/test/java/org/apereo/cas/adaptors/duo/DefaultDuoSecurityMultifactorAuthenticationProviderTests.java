package org.apereo.cas.adaptors.duo;

import module java.base;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import lombok.val;
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
        val authenticationService = mock(DuoSecurityAuthenticationService.class);
        val properties = new DuoSecurityMultifactorAuthenticationProperties();
        properties.getRegistration().setRegistrationUrl("https://www.example.org");
        when(authenticationService.ping()).thenReturn(true);
        when(authenticationService.getProperties()).thenReturn(properties);
        return new DefaultDuoSecurityMultifactorAuthenticationProvider(
            properties.getRegistration(),
            authenticationService);
    }
}
