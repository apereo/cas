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
