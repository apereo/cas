/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import javax.security.auth.login.FailedLoginException;

import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;

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
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(this.fastBindTestConfig.getExistsCredential());
        c.setPassword(this.fastBindTestConfig.getExistsSuccessPassword());

        assertEquals(c.getUsername(), this.fastBindAuthHandler.authenticate(c).getPrincipal().getId());
    }


    public void testSuccessUsernameSaslMd5Password() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(this.saslMd5FastBindTestConfig.getExistsCredential());
        c.setPassword(this.saslMd5FastBindTestConfig.getExistsSuccessPassword());

        assertEquals(c.getUsername(), this.fastBindAuthHandler.authenticate(c).getPrincipal().getId());
    }


    public void testBadUsernamePassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(this.fastBindTestConfig.getExistsCredential());
        c.setPassword(this.fastBindTestConfig.getExistsFailurePassword());

        try {
            this.fastBindAuthHandler.authenticate(c);
            Assert.fail("Should have thrown FailedLoginException");
        } catch (FailedLoginException e) {}
    }


    public void testNotExistsUsername() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(this.fastBindTestConfig.getNotExistsCredential());
        c.setPassword("");

        try {
            this.fastBindAuthHandler.authenticate(c);
            Assert.fail("Should have thrown FailedLoginException");
        } catch (FailedLoginException e) {}
    }
}
