package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class RejectUsersAuthenticationHandlerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() {
        val users = new HashSet<String>();
        users.add("scott");
        users.add("dima");
        users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler("", null, null, users);
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        val c = new UsernamePasswordCredential();

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
            throw new AssertionError("Could not resolve URL.");
        }
    }

    @Test
    public void verifyFailsUserInMap() throws Exception {
        val c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        this.thrown.expect(FailedLoginException.class);

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyPassesUserNotInMap() throws Exception {
        val c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyPassesNullUserName() throws Exception {
        val c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.thrown.expect(AccountNotFoundException.class);


        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyPassesNullUserNameAndPassword() throws Exception {
        this.thrown.expect(AccountNotFoundException.class);


        this.authenticationHandler.authenticate(new UsernamePasswordCredential());
    }
}
