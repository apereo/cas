/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Unit test for {@link BindLdapAuthenticationHandler} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class BindLdapAuthenticationHandlerTests extends
    AbstractDependencyInjectionSpringContextTests {

    protected BindLdapAuthenticationHandler bindAuthHandler;
    protected BindTestConfig bindTestConfig;
    
    public BindLdapAuthenticationHandlerTests() {
        // Switch on field level injection
        setPopulateProtectedVariables(true);
    }
    

    public void testSuccessUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.bindTestConfig.getExistsCredential());
        c.setPassword(this.bindTestConfig.getExistsSuccessPassword());
        
        assertTrue(this.bindAuthHandler.authenticate(c));
    }


    public void testBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.bindTestConfig.getExistsCredential());
        c.setPassword(this.bindTestConfig.getExistsFailurePassword());
        
        assertFalse(this.bindAuthHandler.authenticate(c));
    }


    public void testNotExistsUsername() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.bindTestConfig.getNotExistsCredential());
        c.setPassword("");
        
        assertFalse(this.bindAuthHandler.authenticate(c));
    }

    /**
     * Specifies the Spring configuration to load for this test fixture.
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { "classpath:/ldapContext-test.xml" };
    }
}
