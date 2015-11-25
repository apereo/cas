package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.TestUtils;
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
public final class SimpleTestUsernamePasswordHandlerTests {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(
                TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyValidUsernamePassword() throws Exception {
        final HandlerResult result = authenticationHandler.authenticate(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("SimpleTestUsernamePasswordAuthenticationHandler", result.getHandlerName());
    }

    @Test(expected = FailedLoginException.class)
    public void verifyInvalidUsernamePassword() throws Exception {
        this.authenticationHandler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

}
