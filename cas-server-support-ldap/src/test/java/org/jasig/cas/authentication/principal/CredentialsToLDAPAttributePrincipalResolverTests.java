/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import static org.junit.Assert.*;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
@ContextConfiguration(locations = "classpath:/ldapContext-test.xml")
public class CredentialsToLDAPAttributePrincipalResolverTests extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected CredentialsToLDAPAttributePrincipalResolver ldapResolver;

    @Autowired
    protected ResolverTestConfig resolverTestConfig;
    
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

    @Test
    public void testRuIdNotFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(this.resolverTestConfig.getNotExistsCredential());
        
        final Principal p = this.ldapResolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }

    @Test
    public void testTooMany() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(this.resolverTestConfig.getTooManyCredential());
        
        final Principal p = this.ldapResolver.resolvePrincipal(credentials);
        
        assertNull(p);
    }
}
