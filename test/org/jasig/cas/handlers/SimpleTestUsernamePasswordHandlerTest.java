package org.jasig.cas.handlers;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimpleTestUsernamePasswordHandlerTest extends TestCase {

    private AuthenticationHandler authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();

    public void testValidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test");

        assertTrue(this.authenticationHandler.authenticate(authRequest));
    }

    public void testInvalidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test2");

        assertFalse(this.authenticationHandler.authenticate(authRequest));
    }

    public void testNullUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName(null);
        authRequest.setPassword(null);

        assertFalse(this.authenticationHandler.authenticate(authRequest));
    }
}
