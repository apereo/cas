package org.apereo.cas.authentication.handler.support;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

    private AuthenticationHandler authenticationHandler;

    @BeforeEach
    void initialize() {
        authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    void verifySupportsProperUserCredentials() {
        assertTrue(authenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    void verifySupportsRememberMeUserCredentials() {
        assertTrue(authenticationHandler.supports(new RememberMeUsernamePasswordCredential()));
    }

    @Test
    void verifyDoesntSupportBadUserCredentials() {
        assertFalse(authenticationHandler.supports(CoreAuthenticationTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    void verifyValidUsernamePassword() throws Throwable {
        val result = authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class));
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test
    void verifyInvalidUsernamePassword() {
        assertThrows(FailedLoginException.class,
            () -> authenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(), mock(Service.class)));
    }
}
