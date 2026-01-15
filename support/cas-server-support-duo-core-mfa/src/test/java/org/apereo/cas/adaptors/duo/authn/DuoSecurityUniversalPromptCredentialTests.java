package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityUniversalPromptCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DuoSecurity")
class DuoSecurityUniversalPromptCredentialTests {

    @Test
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val credential = new DuoSecurityUniversalPromptCredential(id, CoreAuthenticationTestUtils.getAuthentication());
        credential.setProviderId(id);
        assertNotNull(credential.getAuthentication());
        assertEquals(id, credential.getId());
        assertEquals(id, credential.getToken());
        assertEquals(id, credential.getProviderId());
    }

}
