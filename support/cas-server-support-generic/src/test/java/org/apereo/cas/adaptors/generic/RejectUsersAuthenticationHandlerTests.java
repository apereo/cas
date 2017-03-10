package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class RejectUsersAuthenticationHandlerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() throws Exception {
        final Set<String> users = new HashSet<>();
        users.add("scott");
        users.add("dima");
        users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler("", null, null, users);
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fff");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                .supports(new HttpBasedServiceCredential(new URL(
                    "http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService())));
        } catch (final MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    @Test
    public void verifyFailsUserInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        this.thrown.expect(FailedLoginException.class);

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyPassesUserNotInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyPassesNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("Username is null.");

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyPassesNullUserNameAndPassword() throws Exception {
        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("Username is null.");

        this.authenticationHandler.authenticate(new UsernamePasswordCredential());
    }
}
