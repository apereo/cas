package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * Test of the simple username/password handler.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class SimpleTestUsernamePasswordHandlerTests {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(CoreAuthenticationTestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyValidUsernamePassword() throws Exception {
        final HandlerResult result = authenticationHandler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test(expected = FailedLoginException.class)
    public void verifyInvalidUsernamePassword() throws Exception {
        this.authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

}
