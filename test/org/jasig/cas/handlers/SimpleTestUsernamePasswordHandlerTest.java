package org.jasig.cas.handlers;

import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;

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
        UsernamePasswordAuthenticationRequest authRequest = new UsernamePasswordAuthenticationRequest();
        authRequest.setUserName("test");
        authRequest.setPassword("test");

        assertTrue(authenticationHandler.authenticate(authRequest));
    }

    public void testInvalidUsernamePassword() {
        UsernamePasswordAuthenticationRequest authRequest = new UsernamePasswordAuthenticationRequest();
        authRequest.setUserName("test");
        authRequest.setPassword("test2");

        assertFalse(authenticationHandler.authenticate(authRequest));
    }

    public void testNullUsernamePassword() {
        UsernamePasswordAuthenticationRequest authRequest = new UsernamePasswordAuthenticationRequest();
        authRequest.setUserName(null);
        authRequest.setPassword(null);

        assertFalse(authenticationHandler.authenticate(authRequest));
    }
}
