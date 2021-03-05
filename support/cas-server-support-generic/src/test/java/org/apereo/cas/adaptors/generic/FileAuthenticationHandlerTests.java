package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("FileSystem")
public class FileAuthenticationHandlerTests {
    private FileAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        this.authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY, null, null, new ClassPathResource("authentication.txt"),
            FileAuthenticationHandler.DEFAULT_SEPARATOR);
        val p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        this.authenticationHandler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(p, mock(ApplicationContext.class)));
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        val c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesNotSupportBadUserCredentials() {
        try {
            val c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService());
            assertFalse(this.authenticationHandler.supports(c));
        } catch (final MalformedURLException e) {
            throw new AssertionError("MalformedURLException caught.");
        }
    }

    @Test
    public void verifyAuthenticatesUserInFileWithDefaultSeparator() throws Exception {
        val c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsUserNotInFileWithDefaultSeparator() {
        val c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

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
        c.setUsername("scott");
        c.setPassword(null);
        assertThrows(FailedLoginException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyAuthenticatesUserInFileWithCommaSeparator() throws Exception {
        val c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY, null, null, new ClassPathResource("authentication2.txt"), ",");
        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsUserNotInFileWithCommaSeparator() {
        val c = new UsernamePasswordCredential();

        this.authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY, null, null,
            new ClassPathResource("authentication2.txt"), ",");
        c.setUsername("fds");
        c.setPassword("rutgers");
        assertThrows(AccountNotFoundException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyFailsGoodUsernameBadPassword() {
        val c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY, null, null,
            new ClassPathResource("authentication2.txt"), ",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        assertThrows(FailedLoginException.class, () -> this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyAuthenticateNoFileName() {
        val c = new UsernamePasswordCredential();
        this.authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY, null, null,
            new ClassPathResource("fff"), FileAuthenticationHandler.DEFAULT_SEPARATOR);

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertThrows(PreventedException.class, () -> this.authenticationHandler.authenticate(c));
    }
}
