package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SwivelMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
public class SwivelMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new SwivelMultifactorAuthenticationProvider("https://www.example.org");
    }

    @Test
    public void verifyPingFails() {
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        val p = new SwivelMultifactorAuthenticationProvider("bad-url");
        assertFalse(p.isAvailable(service));
    }

}
