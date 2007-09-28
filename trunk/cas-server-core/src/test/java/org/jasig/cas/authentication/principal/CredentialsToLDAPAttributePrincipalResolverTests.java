/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.springframework.ldap.support.LdapContextSource;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class CredentialsToLDAPAttributePrincipalResolverTests extends TestCase {

    private CredentialsToLDAPAttributePrincipalResolver resolver;
    
    private UsernamePasswordCredentialsToPrincipalResolver test = new UsernamePasswordCredentialsToPrincipalResolver();

    protected void setUp() throws Exception {
        final LdapContextSource contextSource = new LdapContextSource();
        contextSource.setAnonymousReadOnly(true);
        contextSource.setUrl("ldap://ldap1.rutgers.edu");
        contextSource.afterPropertiesSet();
        
        this.resolver = new CredentialsToLDAPAttributePrincipalResolver();
        this.resolver.setCredentialsToPrincipalResolver(this.test);
        this.resolver.setPrincipalAttributeName("uid");
        this.resolver.setContextSource(contextSource);
        this.resolver.setFilter("rutgersEduIID=%u");
        this.resolver.setSearchBase("ou=people,dc=rutgers,dc=edu");
   }
    
    public void testRuIdFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("SRB54");
        
        assertTrue(this.resolver.supports(credentials));
        
        final Principal p = this.resolver.resolvePrincipal(credentials);
        
        assertNotNull(p);
        assertEquals("battags", p.getId());
    }
    
    public void testRuIdNotFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("SRB");
        
        final Principal p = this.resolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }
    
    public void testTooMany() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("S*");
        
        final Principal p = this.resolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }
}
