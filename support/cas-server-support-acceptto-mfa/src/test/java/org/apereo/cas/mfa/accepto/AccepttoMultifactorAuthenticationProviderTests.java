package org.apereo.cas.mfa.accepto;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccepttoMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class AccepttoMultifactorAuthenticationProviderTests {
    @Test
    public void verifyOperation() {
        val provider = new AccepttoMultifactorAuthenticationProvider();
        assertTrue(provider.isAvailable(RegisteredServiceTestUtils.getRegisteredService()));
        assertNotNull(provider.getFriendlyName());
    }
}
