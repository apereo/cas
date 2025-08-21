package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
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
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class FileAuthenticationHandlerTests {
    private FileAuthenticationHandler authenticationHandler;

    @BeforeEach
    void initialize() {
        authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), new ClassPathResource("authentication.txt"),
            FileAuthenticationHandler.DEFAULT_SEPARATOR);
        val p = new PasswordEncoderProperties();
        p.setType(PasswordEncoderProperties.PasswordEncoderTypes.DEFAULT.name());
        p.setEncodingAlgorithm("MD5");
        p.setCharacterEncoding("UTF-8");
        authenticationHandler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(p, mock(ApplicationContext.class)));
    }

    @Test
    void verifySupportsProperUserCredentials() throws Throwable {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("scott");
        credential.assignPassword("rutgers");
        assertNotNull(authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyDoesNotSupportBadUserCredentials() throws Throwable {
        val credential = new HttpBasedServiceCredential(
            new URI("http://www.rutgers.edu").toURL(), CoreAuthenticationTestUtils.getRegisteredService());
        assertFalse(authenticationHandler.supports(credential));
    }

    @Test
    void verifyAuthenticatesUserInFileWithDefaultSeparator() throws Throwable {
        val credential = new UsernamePasswordCredential();

        credential.setUsername("scott");
        credential.assignPassword("rutgers");

        assertNotNull(authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsUserNotInFileWithDefaultSeparator() {
        val credential = new UsernamePasswordCredential();

        credential.setUsername("fds");
        credential.assignPassword("rutgers");

        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsNullUserName() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername(null);
        credential.assignPassword("user");
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsNullUserNameAndPassword() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername(null);
        credential.assignPassword(null);
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsNullPassword() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("scott");
        credential.assignPassword(null);
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyAuthenticatesUserInFileWithCommaSeparator() throws Throwable {
        val credential = new UsernamePasswordCredential();
        authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), new ClassPathResource("authentication2.txt"), ",");
        credential.setUsername("scott");
        credential.assignPassword("rutgers");
        assertNotNull(authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsUserNotInFileWithCommaSeparator() {
        val credential = new UsernamePasswordCredential();

        authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new ClassPathResource("authentication2.txt"), ",");
        credential.setUsername("fds");
        credential.assignPassword("rutgers");
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyFailsGoodUsernameBadPassword() {
        val credential = new UsernamePasswordCredential();
        authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new ClassPathResource("authentication2.txt"), ",");

        credential.setUsername("scott");
        credential.assignPassword("rutgers1");

        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyAuthenticateNoFileName() {
        val credential = new UsernamePasswordCredential();
        authenticationHandler = new FileAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new ClassPathResource("fff"), FileAuthenticationHandler.DEFAULT_SEPARATOR);

        credential.setUsername("scott");
        credential.assignPassword("rutgers");

        assertThrows(PreventedException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }
}
