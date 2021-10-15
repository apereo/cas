package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityUniversalPromptCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class DuoSecurityUniversalPromptCredentialTests {

    @Test
    public void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val c = new DuoSecurityUniversalPromptCredential(id, CoreAuthenticationTestUtils.getAuthentication());
        c.setProviderId(id);
        assertNotNull(c.getAuthentication());
        assertEquals(id, c.getId());
        assertEquals(id, c.getToken());
        assertEquals(id, c.getProviderId());
    }

}
