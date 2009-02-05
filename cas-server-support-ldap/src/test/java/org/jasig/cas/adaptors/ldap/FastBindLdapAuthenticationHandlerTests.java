/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class FastBindLdapAuthenticationHandlerTests
  extends AbstractDependencyInjectionSpringContextTests {

    protected FastBindLdapAuthenticationHandler fastBindAuthHandler;
    
    public FastBindLdapAuthenticationHandlerTests() {
        // Switch on field level injection
        setPopulateProtectedVariables(true);
    }

    public void testBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("battags");
        c.setPassword("ThisIsObviouslyNotMyRealPassword");
        
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
