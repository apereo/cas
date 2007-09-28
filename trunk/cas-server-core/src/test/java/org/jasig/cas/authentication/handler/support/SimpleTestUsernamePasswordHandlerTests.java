/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordHandlerTests extends TestCase {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
        this.authenticationHandler
            .setPasswordEncoder(new PlainTextPasswordEncoder());
    }

    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    public void testValidUsernamePassword() throws AuthenticationException {
        assertTrue(this.authenticationHandler.authenticate(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testInvalidUsernamePassword() {
        try {
            assertFalse(this.authenticationHandler.authenticate(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword()));
        } catch (AuthenticationException ae) {
            // this is okay
        }
    }

    public void testNullUsernamePassword() {
        try {
            assertFalse(this.authenticationHandler.authenticate(TestUtils
                .getCredentialsWithSameUsernameAndPassword(null)));
        } catch (AuthenticationException ae) {
            // this is okay
        }
    }
    
    public void testAlternateClass() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        assertTrue(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }
    
    public void testAlternateClassWithSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        this.authenticationHandler.setSupportSubClasses(true);
        assertTrue(this.authenticationHandler.supports(new ExtendedCredentials()));
    }
    
    public void testAlternateClassWithNoSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        this.authenticationHandler.setSupportSubClasses(false);
        assertFalse(this.authenticationHandler.supports(new ExtendedCredentials()));
    }
    
    protected class ExtendedCredentials extends UsernamePasswordCredentials {

        private static final long serialVersionUID = 406992293105518363L;
        // nothing to see here
    }
}