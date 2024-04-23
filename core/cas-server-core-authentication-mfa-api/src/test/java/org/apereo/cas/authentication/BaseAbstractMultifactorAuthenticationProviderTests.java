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

    public abstract AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() throws Exception;

    @Test
    void verifyInitialProvider() throws Throwable {
        val p = getMultifactorAuthenticationProvider();
        assertNotNull(p.getId());
        assertNotNull(p.getFriendlyName());
        assertEquals(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED, p.getFailureMode());
    }

    @Test
    void verifyPing() throws Throwable {
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        val p = getMultifactorAuthenticationProvider();
        assertDoesNotThrow(() -> p.isAvailable(service));
    }
}
