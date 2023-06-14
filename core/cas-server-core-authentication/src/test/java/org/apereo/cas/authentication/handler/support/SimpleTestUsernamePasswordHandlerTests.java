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
    public void verifySupportsProperUserCredentials() {
        assertTrue(authenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifySupportsRememberMeUserCredentials() {
        assertTrue(authenticationHandler.supports(new RememberMeUsernamePasswordCredential()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(authenticationHandler.supports(CoreAuthenticationTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyValidUsernamePassword() throws Exception {
        val result = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class));
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test
    public void verifyInvalidUsernamePassword() {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(), mock(Service.class)));
    }
}
