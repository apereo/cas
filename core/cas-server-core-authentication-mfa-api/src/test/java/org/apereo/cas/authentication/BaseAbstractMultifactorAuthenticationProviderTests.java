package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseAbstractMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public abstract class BaseAbstractMultifactorAuthenticationProviderTests {

    public abstract AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider();

    @Test
    public void verifyInitialProvider() {
        val p = getMultifactorAuthenticationProvider();
        assertNotNull(p.getId());
        assertNotNull(p.getFriendlyName());
        assertEquals(RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED, p.getFailureMode());
        val id = p.createUniqueId();
        assertTrue(p.validateId(id));
    }
}
