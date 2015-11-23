package org.jasig.cas.authentication;

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
        assertNotEquals(TestUtils.getCredentialsWithDifferentUsernameAndPassword(), null);
        assertFalse(TestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                TestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(TestUtils.getCredentialsWithDifferentUsernameAndPassword().equals(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }
}
