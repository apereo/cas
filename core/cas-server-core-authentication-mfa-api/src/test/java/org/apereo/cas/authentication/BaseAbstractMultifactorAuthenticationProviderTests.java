package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseAbstractMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public abstract class BaseAbstractMultifactorAuthenticationProviderTests {

    public abstract AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider();

    @Test
    public void verifyInitialProvider() {
        val p = getMultifactorAuthenticationProvider();
        assertNotNull(p.getId());
        assertNotNull(p.getFriendlyName());
        assertEquals(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED, p.getFailureMode());
    }

    @Test
    public void verifyPing() {
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        val p = getMultifactorAuthenticationProvider();
        assertDoesNotThrow(() -> p.isAvailable(service));
    }
}
