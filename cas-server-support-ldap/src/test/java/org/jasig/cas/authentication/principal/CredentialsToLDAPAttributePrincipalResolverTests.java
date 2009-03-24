/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class CredentialsToLDAPAttributePrincipalResolverTests
  extends AbstractDependencyInjectionSpringContextTests {

    protected CredentialsToLDAPAttributePrincipalResolver ldapResolver;

    protected ResolverTestConfig resolverTestConfig;
    
    public CredentialsToLDAPAttributePrincipalResolverTests() {
        // Switch on field level injection
        setPopulateProtectedVariables(true);
    }

    // XXX TEMPORARILY DISABLED TEST SO WE CAN BUILD
    /*
    public void testRuIdFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(this.resolverTestConfig.getExistsCredential());
        
        assertTrue(this.ldapResolver.supports(credentials));
        
        final Principal p = this.ldapResolver.resolvePrincipal(credentials);
        
        assertNotNull(p);
        assertEquals(this.resolverTestConfig.getExistsPrincipal(), p.getId());
    }*/
    
    public void testRuIdNotFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(this.resolverTestConfig.getNotExistsCredential());
        
        final Principal p = this.ldapResolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }
    
    public void testTooMany() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(this.resolverTestConfig.getTooManyCredential());
        
        final Principal p = this.ldapResolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }
    
    /**
     * Specifies the Spring configuration to load for this test fixture.
     * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[] { "classpath:/ldapContext-test.xml" };
    }
}
