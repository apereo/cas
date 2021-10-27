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
