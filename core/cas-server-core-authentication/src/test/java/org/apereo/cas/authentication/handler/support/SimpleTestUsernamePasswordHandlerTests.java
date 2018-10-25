package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the simple username/password handler.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class SimpleTestUsernamePasswordHandlerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @BeforeEach
    public void initialize() {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifySupportsRememberMeUserCredentials() {
        assertTrue(this.authenticationHandler.supports(new RememberMeUsernamePasswordCredential()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(CoreAuthenticationTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyValidUsernamePassword() throws Exception {
        val result =
            authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test
    public void verifyInvalidUsernamePassword() throws Exception {
        this.thrown.expect(FailedLoginException.class);

        this.authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }
}
