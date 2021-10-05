package org.apereo.cas.adaptors.duo.authn;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityAuthenticationResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class DuoSecurityAuthenticationResultTests {
    @Test
    public void verifyOperation() {
        val result = DuoSecurityAuthenticationResult.builder().success(true).username("casuser").build();
        assertNotNull(result.getUsername());
        assertNotNull(result.getAttributes());
        assertNotNull(result.toString());
        assertTrue(result.isSuccess());
    }
}
