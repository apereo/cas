package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;

import org.junit.jupiter.api.Tag;

/**
 * This is {@link U2FMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class U2FMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {

    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new U2FMultifactorAuthenticationProvider();
    }
}
