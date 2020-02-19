package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;

import org.junit.jupiter.api.Tag;

/**
 * This is {@link SwivelMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class SwivelMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new SwivelMultifactorAuthenticationProvider("https://www.example.org");
    }
}
