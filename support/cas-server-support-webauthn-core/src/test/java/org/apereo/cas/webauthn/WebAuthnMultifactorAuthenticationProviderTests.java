package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.junit.jupiter.api.Tag;

/**
 * This is {@link WebAuthnMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
class WebAuthnMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new WebAuthnMultifactorAuthenticationProvider();
    }
}
