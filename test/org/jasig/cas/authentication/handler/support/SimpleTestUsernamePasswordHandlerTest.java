package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.adaptors.cas.LegacyCasTrustedCredentials;
import org.jasig.cas.authentication.AuthenticationException;
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

    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }
    
    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(new LegacyCasTrustedCredentials()));
    }
    
    public void testValidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test");

        try {
            assertTrue(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
            fail();
        }
    }

    public void testInvalidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test2");

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
        	// this is okay
        }
    }

    public void testNullUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName(null);
        authRequest.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
            // this is okay
        }
    }
}
