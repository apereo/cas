package org.apereo.cas.webauthn;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class WebAuthnCredentialTests {
    @Test
    public void verifyOperation() {
        val input = new WebAuthnCredential(UUID.randomUUID().toString());
        assertNotNull(input.getToken());
    }

}
