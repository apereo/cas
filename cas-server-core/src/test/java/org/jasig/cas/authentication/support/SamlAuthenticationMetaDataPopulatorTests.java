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
package org.jasig.cas.authentication.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.UsernamePasswordCredential;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class SamlAuthenticationMetaDataPopulatorTests extends TestCase {

    private SamlAuthenticationMetaDataPopulator populator;

    protected void setUp() throws Exception {
        this.populator = new SamlAuthenticationMetaDataPopulator();
        super.setUp();
    }
    
    public void testAuthenticationTypeFound() {
        final UsernamePasswordCredential credentials = new UsernamePasswordCredential();
        final MutableAuthentication ma = TestUtils.newMutableAuthentication(TestUtils.getPrincipal());
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertEquals(m2.getAttributes().get("samlAuthenticationStatementAuthMethod"), SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD);
    }
    
    public void testAuthenticationTypeNotFound() {
        final CustomCredential credentials = new CustomCredential();
        final MutableAuthentication ma = TestUtils.newMutableAuthentication(TestUtils.getPrincipal());
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertNull(m2.getAttributes().get("samlAuthenticationStatementAuthMethod"));
    }
    
    public void testAuthenticationTypeFoundCustom() {
        final CustomCredential credentials = new CustomCredential();
        
        final Map<String, String> added = new HashMap<String, String>();
        added.put(CustomCredential.class.getName(), "FF");
        
        this.populator.setUserDefinedMappings(added);

        final MutableAuthentication ma = TestUtils.newMutableAuthentication(TestUtils.getPrincipal());
        final Authentication m2 = this.populator.populateAttributes(ma, credentials);
        
        assertEquals("FF", m2.getAttributes().get("samlAuthenticationStatementAuthMethod"));
    }
    
    protected class CustomCredential implements Credential {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -3387599342233073148L;
        // nothing to do
    }
}
