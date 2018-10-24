package org.apereo.cas.digest.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DigestAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DigestAuthenticationUtilsTests {

    @Test
    public void verifyNonce() {
        assertNotNull(DigestAuthenticationUtils.createNonce());
    }

    @Test
    public void verifyCNonce() {
        assertNotNull(DigestAuthenticationUtils.createCnonce());
    }

    @Test
    public void verifyOpaque() {
        assertNotNull(DigestAuthenticationUtils.createOpaque("domain", DigestAuthenticationUtils.createNonce()));
    }

    @Test
    public void verifyHeader() {
        val header = DigestAuthenticationUtils.createAuthenticateHeader("domain",
            "authMethod", DigestAuthenticationUtils.createNonce());
        assertNotNull(header);

        assertTrue(header.contains("nonce="));
        assertTrue(header.contains("opaque="));
        assertTrue(header.contains("qop="));
    }
}
