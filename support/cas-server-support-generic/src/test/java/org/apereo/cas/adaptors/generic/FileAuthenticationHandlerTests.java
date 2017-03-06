package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.support.Beans;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FileAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new FileAuthenticationHandler("", null, null, new ClassPathResource("authentication.txt"),
                FileAuthenticationHandler.DEFAULT_SEPARATOR);
        final PasswordEncoderProperties p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        this.authenticationHandler.setPasswordEncoder(Beans.newPasswordEncoder(p));
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesNotSupportBadUserCredentials() {
        try {
            final HttpBasedServiceCredential c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService());
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

    @Test
    public void verifyFailsUserNotInFileWithDefaultSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("fds not found in backing file.");

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
        c.setUsername("scott");
        c.setPassword(null);
        this.thrown.expect(FailedLoginException.class);
        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyAuthenticatesUserInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler("", null, null, new ClassPathResource("authentication2.txt"), ",");
        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsUserNotInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler = new FileAuthenticationHandler("", null, null, new ClassPathResource("authentication2.txt"), ",");
        c.setUsername("fds");
        c.setPassword("rutgers");
        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("fds not found in backing file.");
        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyFailsGoodUsernameBadPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler("", null, null, new ClassPathResource("authentication2.txt"), ",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        this.thrown.expect(FailedLoginException.class);

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyAuthenticateNoFileName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler("", null, null, new ClassPathResource("fff"), FileAuthenticationHandler.DEFAULT_SEPARATOR);

        c.setUsername("scott");
        c.setPassword("rutgers");

        this.thrown.expect(PreventedException.class);
        this.thrown.expectMessage("IO error reading backing file");

        this.authenticationHandler.authenticate(c);
    }
}
