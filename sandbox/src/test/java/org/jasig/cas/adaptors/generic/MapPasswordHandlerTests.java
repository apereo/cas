/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class MapPasswordHandlerTests extends TestCase {

    private Map users;

    private MapPasswordHandler authenticationHandler;

    protected void setUp() {
        this.users = new HashMap();

        this.users.put("scott", "rutgers");
        this.users.put("dima", "javarules");
        this.users.put("bill", "thisisAwesoME");

        this.authenticationHandler = new MapPasswordHandler();

        this.authenticationHandler.setUsernamesToPasswords(this.users);
    }

    /**
     * Test that we can authenticate a correct username and password.
     * @throws AuthenticationException
     */
    public void testSupportsProperUserCredentials() throws AuthenticationException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        assertTrue(this.authenticationHandler.supports(c));
        
        c.setUsername("scott");
        c.setPassword("rutgers");
        
        assertTrue(this.authenticationHandler.authenticate(c));
    }

    /**
     * Test that MapPasswordHandler throws UnsupportedCredentialsException when
     * we try to authenticate unsupported credentials.
     * @throws AuthenticationException
     */
    public void testDoesntSupportBadUserCredentials() throws AuthenticationException {
        
        assertFalse(this.authenticationHandler.supports(new UnsupportedCredentials()));
        
        try {
            this.authenticationHandler
                .authenticate(new UnsupportedCredentials());
        } catch (UnsupportedCredentialsException e) {
            // expected
            return;
        }
    }

    /**
     * Test that MapPasswordHandler.authenticate() returns false when presented
     * with UsernamePasswordCredentials for a user not present in its backing Map.
     * @throws AuthenticationException
     */
    public void testFailsUserNotInMap() throws AuthenticationException {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("fds");
        c.setPassword("rutgers");

        assertFalse(this.authenticationHandler.authenticate(c));

    }

    /**
     * Test that 
     * {@link MapPasswordHandler#authenticateUsernamePasswordInternal(UsernamePasswordCredentials)}
     * returns false for UsernamePasswordCredentials presenting a null username.
     * @throws AuthenticationException
     */
    public void testFailsNullUserName() throws AuthenticationException {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername(null);
        c.setPassword("user");

        assertFalse(this.authenticationHandler.authenticate(c));
        
    }

    /**
     * Test that 
     * {@link MapPasswordHandler#authenticateUsernamePasswordInternal(UsernamePasswordCredentials)}
     * returns false for UsernamePasswordCredentials presenting a null username and a null password.
     * @throws AuthenticationException
     */
    public void testFailsNullUserNameAndPassword() throws AuthenticationException {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername(null);
        c.setPassword(null);

        assertFalse(this.authenticationHandler.authenticate(c));
            
    }

    /**
     * Test that 
     * {@link MapPasswordHandler#authenticateUsernamePasswordInternal(UsernamePasswordCredentials)}
     * returns false for UsernamePasswordCredentials presenting a null password.
     * @throws AuthenticationException
     */
    public void testFailsNullPassword() throws AuthenticationException {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("scott");
        c.setPassword(null);

        assertFalse(this.authenticationHandler.authenticate(c));
    }

    /**
     * Test that attempting to set the backing Map of a MapPasswordHandler
     * to null yields an IllegalArgumentException.
     */
    public void testSettingNullUsernamesToPasswords() {
        try {
            this.authenticationHandler.setUsernamesToPasswords(null);
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException expected
            return;
        }
        fail("Should have thrown an IllegalArgumentException.");
    }
    
    /**
     * Test that afterPropertiesSet() returns normally when the backing store
     * has been properly set.
     * @throws Exception
     */
    public void testAfterPropertiesSetWithUsernamesToPasswords() throws Exception {
        this.authenticationHandler.afterPropertiesSet();
    }
    
    /**
     * Test that afterPropertiesSet() throws IllegalStateException when invoked 
     * on a MapPasswordHandler which has not had a Map of usernames to passwords
     * injected into it.
     * @throws Exception
     */
    public void testAfterPropertiesSetNoUsernamesToPasswords() throws Exception {
        
        MapPasswordHandler bareHandler = new MapPasswordHandler();
        
        try {
            bareHandler.afterPropertiesSet();
        } catch (IllegalStateException ise) {
            // expected
            return;
        }
        fail("Should have thrown IllegalStateException since no backing Map was injected.");
    }
    
    /**
     * Test that changes to the backing Map are reflected on subsequent 
     * MapPasswordHandler invocations.
     * @throws AuthenticationException
     */
    public void testChangingBackingMap() throws AuthenticationException {
    
        UsernamePasswordCredentials scottRutgers = new UsernamePasswordCredentials();
        
        scottRutgers.setUsername("scott");
        scottRutgers.setPassword("rutgers");
        
        UsernamePasswordCredentials drewYale = new UsernamePasswordCredentials();
        drewYale.setUsername("aam26");
        drewYale.setPassword("yale");
        
        // scottRutgers is currently valid credentials
        assertTrue(this.authenticationHandler.authenticate(scottRutgers));
        // drewYale is currently not valid credentials
        assertFalse(this.authenticationHandler.authenticate(drewYale));
        
        // add Drew to the backing map
        this.users.put("aam26", "yale");
        // change Scott's password
        this.users.put("scott", "yale");
        
        
        // scottRutgers is no longer valid credentials
        assertFalse(this.authenticationHandler.authenticate(scottRutgers));
        // drewYale is now valid credentials
        assertTrue(this.authenticationHandler.authenticate(drewYale));
        
    }
    
    /**
     * Test that we can overwrite the backing Map.
     * @throws AuthenticationException
     */
    public void testOverrideBackingMap() throws AuthenticationException {
        UsernamePasswordCredentials scottRutgers = new UsernamePasswordCredentials();
        
        scottRutgers.setUsername("scott");
        scottRutgers.setPassword("rutgers");
        
        UsernamePasswordCredentials drewYale = new UsernamePasswordCredentials();
        drewYale.setUsername("aam26");
        drewYale.setPassword("yale");
        
        // scottRutgers is currently valid credentials
        assertTrue(this.authenticationHandler.authenticate(scottRutgers));
        // drewYale is currently not valid credentials
        assertFalse(this.authenticationHandler.authenticate(drewYale));
        
        // create a new Map from usernames to passwords that only contains
        // drew's credentials
        Map newMap = new HashMap();
        newMap.put("aam26", "yale");
        
        // inject the new map
        
        this.authenticationHandler.setUsernamesToPasswords(newMap);
        
        
        // scottRutgers is no longer valid credentials
        assertFalse(this.authenticationHandler.authenticate(scottRutgers));
        // drewYale is now valid credentials
        assertTrue(this.authenticationHandler.authenticate(drewYale));
    }
    
    /**
     * An ad-hoc credentials that MapPasswordHandler ought not to support.
     */
    private class UnsupportedCredentials implements Credentials {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7166496244172616112L;
        // does nothing
    }
}