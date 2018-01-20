package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
public class AcceptUsersAuthenticationHandlerTests {

    private static final String SCOTT = "scott";
    private static final String RUTGERS = "rutgers";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final AcceptUsersAuthenticationHandler authenticationHandler;

    public AcceptUsersAuthenticationHandlerTests() {
        final Map<String, String> users = new HashMap<>();
        users.put(SCOTT, RUTGERS);
        users.put("dima", "javarules");
        users.put("bill", "thisisAwesoME");
        users.put("brian", "t�st");

        this.authenticationHandler = new AcceptUsersAuthenticationHandler("", null, new DefaultPrincipalFactory(), null, users);
    }

    @Test
    public void verifySupportsSpecialCharacters() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername("brian");
        c.setPassword("t�st");
        assertEquals("brian", this.authenticationHandler.authenticate(c).getPrincipal().getId());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(RUTGERS);
        assertTrue(this.authenticationHandler.supports(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                    .supports(new HttpBasedServiceCredential(new URL(
                            "http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService("https://some.app.edu"))));
        } catch (final MalformedURLException e) {
            throw new AssertionError("Could not resolve URL.", e);
        }
    }

    @Test
    public void verifyAuthenticatesUserInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(RUTGERS);

        try {
            assertEquals(SCOTT, this.authenticationHandler.authenticate(c).getPrincipal().getId());
        } catch (final GeneralSecurityException e) {
            throw new AssertionError("Authentication exception caught but it should not have been thrown.", e);
        }
    }

    @Test
    public void verifyFailsUserNotInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword(RUTGERS);

        this.thrown.expect(AccountNotFoundException.class);


        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyFailsNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.thrown.expect(AccountNotFoundException.class);
        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyFailsNullUserNameAndPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);

        this.thrown.expect(AccountNotFoundException.class);


        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyFailsNullPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(null);

        this.thrown.expect(FailedLoginException.class);


        this.authenticationHandler.authenticate(c);
    }
}
