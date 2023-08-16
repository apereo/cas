package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test of the simple username/password handler.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class SimpleTestUsernamePasswordHandlerTests {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    void verifySupportsProperUserCredentials() throws Throwable {
        assertTrue(authenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifySupportsRememberMeUserCredentials() throws Throwable {
        assertTrue(authenticationHandler.supports(new RememberMeUsernamePasswordCredential()));
    }

    @Test
    void verifyDoesntSupportBadUserCredentials() throws Throwable {
        assertFalse(authenticationHandler.supports(CoreAuthenticationTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    void verifyValidUsernamePassword() throws Throwable {
        val result = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class));
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test
    void verifyInvalidUsernamePassword() throws Throwable {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(), mock(Service.class)));
    }
}
