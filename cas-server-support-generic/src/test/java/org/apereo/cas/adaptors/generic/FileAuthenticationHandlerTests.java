package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class FileAuthenticationHandlerTests {

    private FileAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new FileAuthenticationHandler();
        this.authenticationHandler.setFileName(
                new ClassPathResource("org/apereo/cas/adaptors/generic/authentication.txt"));

    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            final HttpBasedServiceCredential c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"), TestUtils.getRegisteredService());
            assertFalse(this.authenticationHandler.supports(c));
        } catch (final MalformedURLException e) {
            fail("MalformedURLException caught.");
        }
    }

    @Test
    public void verifyAuthenticatesUserInFileWithDefaultSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsUserNotInFileWithDefaultSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsNullUserNameAndPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = FailedLoginException.class)
    public void verifyFailsNullPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword(null);

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyAuthenticatesUserInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/apereo/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsUserNotInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/apereo/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("fds");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = FailedLoginException.class)
    public void verifyFailsGoodUsernameBadPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/apereo/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = PreventedException.class)
    public void verifyAuthenticateNoFileName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler.setFileName(new ClassPathResource("fff"));

        c.setUsername("scott");
        c.setPassword("rutgers");

        this.authenticationHandler.authenticate(c);
    }
}
