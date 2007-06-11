/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.adaptors.ldap.util.AuthenticatedLdapContextSource;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class FastBindLdapAuthenticationHandlerTests extends TestCase {

    private FastBindLdapAuthenticationHandler handler;

    protected void setUp() throws Exception {
        final AuthenticatedLdapContextSource contextSource = new AuthenticatedLdapContextSource();
        contextSource.setUrl("ldap://ldap1.rutgers.edu");
        contextSource.afterPropertiesSet();
        
        this.handler = new FastBindLdapAuthenticationHandler();
        this.handler.setContextSource(contextSource);
        this.handler.setFilter("uid=%u,ou=people,dc=rutgers,dc=edu");
        this.handler.afterPropertiesSet();
    }
    
    public void testBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("battags");
        c.setPassword("ThisIsObviouslyNotMyRealPassword");
        
        assertFalse(this.handler.authenticate(c));
    }
    
    
    
}
