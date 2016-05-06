package org.apereo.cas.authentication;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for RememberMeUsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RememberMePasswordCredentialTests {

    @Test
    public void verifyGettersAndSetters() {
        final RememberMeUsernamePasswordCredential c = new RememberMeUsernamePasswordCredential();
        c.setPassword("password");
        c.setUsername("username");
        c.setRememberMe(true);

        assertEquals("username", c.getUsername());
        assertEquals("password", c.getPassword());
        assertTrue(c.isRememberMe());
    }
}
