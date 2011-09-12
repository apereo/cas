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
 * Unit test for {@link FastBindLdapAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
@ContextConfiguration(locations = "classpath:/ldapContext-test.xml")
public class FastBindLdapAuthenticationHandlerTests extends AbstractJUnit4SpringContextTests {

    protected FastBindLdapAuthenticationHandler fastBindAuthHandler;
    protected FastBindLdapAuthenticationHandler saslMd5FastBindAuthHandler;
    protected BindTestConfig fastBindTestConfig;
    protected BindTestConfig saslMd5FastBindTestConfig;


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
}
