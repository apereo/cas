package org.apereo.cas.support.inwebo;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class InweboMultifactorAuthenticationProviderTests {
    @Test
    public void verifyOperation() {
        val provider = new InweboMultifactorAuthenticationProvider();
        assertNotNull(provider.getId());
        assertNotNull(provider.getFriendlyName());
    }

}
