package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class AcceptUsersAuthenticationHandlerTests {
    private static final String SCOTT = "scott";
    private static final String RUTGERS = "rutgers";

    private final AcceptUsersAuthenticationHandler authenticationHandler;

    public AcceptUsersAuthenticationHandlerTests() {
        val users = new HashMap<String, String>();
        users.put(SCOTT, RUTGERS);
        users.put("dima", "javarules");
        users.put("bill", "thisisAwesoME");
        users.put("brian", "t�st");

        this.authenticationHandler = new AcceptUsersAuthenticationHandler(StringUtils.EMPTY, null, PrincipalFactoryUtils.newPrincipalFactory(), null, users);
    }

    @Test
    @SneakyThrows
    public void verifySupportsSpecialCharacters() {
        val c = new UsernamePasswordCredential();
        c.setUsername("brian");
        c.setPassword("t�st");
        assertEquals("brian", this.authenticationHandler.authenticate(c).getPrincipal().getId());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        val c = new UsernamePasswordCredential();

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
    @SneakyThrows
    public void verifyAuthenticatesUserInMap() {
        val c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(RUTGERS);

        try {
            assertEquals(SCOTT, this.authenticationHandler.authenticate(c).getPrincipal().getId());
        } catch (final GeneralSecurityException e) {
            throw new AssertionError("Authentication exception caught but it should not have been thrown.", e);
        }
    }

    @Test
    public void verifyFailsUserNotInMap() {
        val c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword(RUTGERS);

        assertThrows(AccountNotFoundException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsNullUserName() {
        val c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        assertThrows(AccountNotFoundException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsNullUserNameAndPassword() {
        val c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);

        assertThrows(AccountNotFoundException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsNullPassword() {
        val c = new UsernamePasswordCredential();

        c.setUsername(SCOTT);
        c.setPassword(null);

        assertThrows(FailedLoginException.class, () -> this.authenticationHandler.authenticate(c));
    }
}
