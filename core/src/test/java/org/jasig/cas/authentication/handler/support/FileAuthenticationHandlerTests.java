/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class FileAuthenticationHandlerTests extends TestCase {

    private FileAuthenticationHandler authenticationHandler;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.authenticationHandler = new FileAuthenticationHandler();
        this.authenticationHandler.setFileName("authentication.txt");

    }

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword("rutgers");
        try {
            this.authenticationHandler.authenticate(c);
        }
        catch (UnsupportedCredentialsException e) {
            fail("UnsupportedCredentialsException caught");
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.rutgers.edu"));
            this.authenticationHandler.authenticate(c);
        }
        catch (MalformedURLException e) {
            fail("MalformedURLException caught.");
        }
        catch (UnsupportedCredentialsException e) {
            // this is okay
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testAuthenticatesUserInFileWithDefaultSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    public void testFailsUserNotInFileWithDefaultSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("fds");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserName() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName(null);
        c.setPassword("user");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserNameAndPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName(null);
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testAuthenticatesUserInFileWithCommaSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        this.authenticationHandler.setFileName("authentication2.txt");
        this.authenticationHandler.setSeparator(",");

        c.setUserName("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    public void testFailsUserNotInFileWithCommaSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        this.authenticationHandler.setFileName("authentication2.txt");
        this.authenticationHandler.setSeparator(",");

        c.setUserName("fds");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testFailsGoodUsernameBadPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        this.authenticationHandler.setFileName("authentication2.txt");
        this.authenticationHandler.setSeparator(",");

        c.setUserName("scott");
        c.setPassword("rutgers1");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testAfterPropertiesSetNoPasswordTranslator() {
        this.authenticationHandler.setPasswordTranslator(null);
        
        try {
            this.authenticationHandler.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            // this is good
        }
    }
    
    public void testAfterProperties() {
        
        try {
            this.authenticationHandler.afterPropertiesSet();

        } catch (Exception e) {
            fail("No Exception expected.");
        }
    }
    
    public void testAfterPropertiesSetNoFileName() {
        this.authenticationHandler.setFileName(null);
        
        try {
            this.authenticationHandler.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            // this is good
        }
    }
    
    public void testAfterPropertiesSetNoSeperator() {
        this.authenticationHandler.setSeparator(null);
        
        try {
            this.authenticationHandler.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            // this is good
        }
    }
    
    public void testAuthenticateNoFileName() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        this.authenticationHandler.setFileName("fff");
        
        c.setUserName("scott");
        c.setPassword("rutgers");
        
        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (Exception e) {
            // this is good
        }
    }
}