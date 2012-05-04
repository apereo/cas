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
