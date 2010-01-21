/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import static org.junit.Assert.*;


/**
 * Unit test for {@link BindLdapAuthenticationHandler} class.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
@ContextConfiguration(locations = "classpath:/ldapContext-test.xml")
public class BindLdapAuthenticationHandlerTests extends AbstractJUnit4SpringContextTests {

    protected BindLdapAuthenticationHandler bindAuthHandler;
    protected BindTestConfig bindTestConfig;

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
}
