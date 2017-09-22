package org.apereo.cas.authentication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class UsernamePasswordCredentialTests {

    @Test
    public void verifySetGetUsername() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        final String userName = "test";

        c.setUsername(userName);

        assertEquals(userName, c.getUsername());
    }

    @Test
    public void verifySetGetPassword() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        final String password = "test";

        c.setPassword(password);

        assertEquals(password, c.getPassword());
    }

    @Test
    public void verifyEquals() {
        assertNotEquals(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(), null);
        assertFalse(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }
}
