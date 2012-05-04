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
package org.jasig.cas.web.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.SamlAuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.jasig.cas.web.view.Cas10ResponseViewTests.MockWriterHttpMockHttpServletResponse;
import org.opensaml.SAMLAuthenticationStatement;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class Saml10SuccessResponseViewTests extends TestCase {

    private Saml10SuccessResponseView response;
    
    protected void setUp() throws Exception {
        this.response = new Saml10SuccessResponseView();
        this.response.setIssuer("testIssuer");
        this.response.setIssueLength(1000);
        super.setUp();
    }

    public void testResponse() throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("testAttribute", "testValue");
        attributes.put("testEmptyCollection", Collections.emptyList());
        attributes.put("testAttributeCollection", Arrays.asList(new String[] {"tac1", "tac2"}));
        final SimplePrincipal principal = new SimplePrincipal("testPrincipal", attributes);
        
        final MutableAuthentication authentication = new MutableAuthentication(principal);
        authentication.getAttributes().put(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD, SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client);
        authentication.getAttributes().put("testSamlAttribute", "value");
        
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        final MockWriterHttpMockHttpServletResponse servletResponse = new MockWriterHttpMockHttpServletResponse();
        
        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getWrittenValue();
        
        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains("testAttribute"));
        assertTrue(written.contains("testValue"));
        assertFalse(written.contains("testEmptyCollection"));
        assertTrue(written.contains("testAttributeCollection"));
        assertTrue(written.contains("tac1"));
        assertTrue(written.contains("tac2"));
        assertTrue(written.contains(SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client));
        assertTrue(written.contains("AuthenticationMethod"));
    }
    
    public void testResponseWithNoAttributes() throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final SimplePrincipal principal = new SimplePrincipal("testPrincipal");
        
        final MutableAuthentication authentication = new MutableAuthentication(principal);
        authentication.getAttributes().put(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD, SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client);
        authentication.getAttributes().put("testSamlAttribute", "value");
        
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        final MockWriterHttpMockHttpServletResponse servletResponse = new MockWriterHttpMockHttpServletResponse();
        
        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getWrittenValue();
        
        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains(SAMLAuthenticationStatement.AuthenticationMethod_SSL_TLS_Client));
        assertTrue(written.contains("AuthenticationMethod"));
    }
    
    public void testResponseWithoutAuthMethod() throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("testAttribute", "testValue");
        final SimplePrincipal principal = new SimplePrincipal("testPrincipal", attributes);
        
        final MutableAuthentication authentication = new MutableAuthentication(principal);
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        final MockWriterHttpMockHttpServletResponse servletResponse = new MockWriterHttpMockHttpServletResponse();
        
        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getWrittenValue();
        
        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains("testAttribute"));
        assertTrue(written.contains("testValue"));
        assertTrue(written.contains("urn:oasis:names:tc:SAML:1.0:am:unspecified"));       
    }
    
    public void testException() {
        this.response.setIssuer(null);
        
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final Map<String, Object> attributes = new HashMap<String, Object>();
        final SimplePrincipal principal = new SimplePrincipal("testPrincipal", attributes);
        
        final MutableAuthentication authentication = new MutableAuthentication(principal);
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        try {
            this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), new MockHttpServletResponse());
            fail("Exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}
