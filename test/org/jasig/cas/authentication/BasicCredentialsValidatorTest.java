/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class BasicCredentialsValidatorTest extends TestCase {
    private Validator validator = new BasicCredentialsValidator();
    
    public void testSupportsUsernamePasswordCredentials() {
        assertTrue(this.validator.supports(UsernamePasswordCredentials.class));
    }
    
    public void testNotSupportsBasicHttpServiceCredentials() {
        assertFalse(this.validator.supports(HttpBasedServiceCredentials.class));
    }
    
    public void testValidationPasses() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName("test");
        c.setPassword("test");
        
        this.validator.validate(c, b);
        
        assertFalse(b.hasErrors());
    }
    
    public void testValidationFailsPasswordNull() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName("test");
        c.setPassword(null);
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 1);
    }
    
    public void testValidationFailsPasswordBlank() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName("test");
        c.setPassword("");
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 1);
    }

    public void testValidationFailsUsernameNull() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName(null);
        c.setPassword("hello");
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 1);
    }
    
    public void testValidationFailsUsernameBlank() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName("");
        c.setPassword("hello");
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 1);
    }

    public void testValidationFailsUsernameAndPasswordBlank() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName("");
        c.setPassword("");
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 2);
    }

    public void testValidationFailsUsernameAndPasswordNull() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final BindException b = new BindException(c, "credentials");
        c.setUserName(null);
        c.setPassword(null);
        
        this.validator.validate(c, b);
        
        assertTrue(b.hasErrors());
        assertEquals(b.getErrorCount(), 2);
    }
}