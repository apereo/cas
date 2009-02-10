/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Unit test for {@link FastBindLdapAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class FastBindLdapAuthenticationHandlerTests
  extends AbstractDependencyInjectionSpringContextTests {

    protected FastBindLdapAuthenticationHandler fastBindAuthHandler;
    protected FastBindLdapAuthenticationHandler saslMd5FastBindAuthHandler;
    protected BindTestConfig fastBindTestConfig;
    protected BindTestConfig saslMd5FastBindTestConfig;
    
    public FastBindLdapAuthenticationHandlerTests() {
        // Switch on field level injection
        setPopulateProtectedVariables(true);
    }
    

    public void testSuccessUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.fastBindTestConfig.getExistsCredential());
        c.setPassword(this.fastBindTestConfig.getExistsSuccessPassword());
        
        assertTrue(this.fastBindAuthHandler.authenticate(c));
    }


    public void testSuccessUsernameSaslMd5Password() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.saslMd5FastBindTestConfig.getExistsCredential());
        c.setPassword(this.saslMd5FastBindTestConfig.getExistsSuccessPassword());
        
        assertTrue(this.saslMd5FastBindAuthHandler.authenticate(c));
    }


    public void testBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.fastBindTestConfig.getExistsCredential());
        c.setPassword(this.fastBindTestConfig.getExistsFailurePassword());
        
        assertFalse(this.fastBindAuthHandler.authenticate(c));
    }


    public void testNotExistsUsername() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername(this.fastBindTestConfig.getNotExistsCredential());
        c.setPassword("");
        
        assertFalse(this.fastBindAuthHandler.authenticate(c));
    }

    /**
     * Specifies the Spring configuration to load for this test fixture.
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { "classpath:/ldapContext-test.xml" };
    }
    
}
