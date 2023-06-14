package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RememberMeUsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Tag("Authentication")
class RememberMePasswordCredentialTests {

    @Test
    void verifyGettersAndSetters() {
        val c = new RememberMeUsernamePasswordCredential();
        c.assignPassword("password");
        c.setUsername("username");
        c.setRememberMe(true);

        assertEquals("username", c.getUsername());
        assertEquals("password", c.toPassword());
        assertTrue(c.isRememberMe());
    }
}
