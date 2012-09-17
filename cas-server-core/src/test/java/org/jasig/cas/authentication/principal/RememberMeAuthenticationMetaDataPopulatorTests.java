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

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.MutableAuthentication;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public class RememberMeAuthenticationMetaDataPopulatorTests extends TestCase {
    
    private RememberMeAuthenticationMetaDataPopulator p  = new RememberMeAuthenticationMetaDataPopulator();

    public void testWithTrueRememberMeCredentials() {
        final Authentication auth = new MutableAuthentication(TestUtils.getPrincipal());
        final RememberMeUsernamePasswordCredentials c = new RememberMeUsernamePasswordCredentials();
        c.setRememberMe(true);
        
        final Authentication auth2 = this.p.populateAttributes(auth, c);
        
        assertEquals(Boolean.TRUE, auth2.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }
    
    public void testWithFalseRememberMeCredentials() {
        final Authentication auth = new MutableAuthentication(TestUtils.getPrincipal());
        final RememberMeUsernamePasswordCredentials c = new RememberMeUsernamePasswordCredentials();
        c.setRememberMe(false);
        
        final Authentication auth2 = this.p.populateAttributes(auth, c);
        
        assertNull(auth2.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    
    public void testWithoutRememberMeCredentials() {
        final Authentication auth = new MutableAuthentication(TestUtils.getPrincipal());        
        final Authentication auth2 = this.p.populateAttributes(auth, TestUtils.getCredentialsWithSameUsernameAndPassword());
        
        assertNull(auth2.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }
    

}
