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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for {@link Saml10SuccessResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
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
        authentication.getAttributes().put(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD, SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authentication.getAttributes().put("testSamlAttribute", "value");
        
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();

        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains("testAttribute"));
        assertTrue(written.contains("testValue"));
        assertFalse(written.contains("testEmptyCollection"));
        assertTrue(written.contains("testAttributeCollection"));
        assertTrue(written.contains("tac1"));
        assertTrue(written.contains("tac2"));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
        assertTrue(written.contains("AuthenticationMethod"));
        assertTrue(written.contains("AssertionID"));
    }
    
    public void testResponseWithNoAttributes() throws Exception {
        final Map<String, Object> model = new HashMap<String, Object>();
        
        final SimplePrincipal principal = new SimplePrincipal("testPrincipal");
        
        final MutableAuthentication authentication = new MutableAuthentication(principal);
        authentication.getAttributes().put(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD, SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT);
        authentication.getAttributes().put("testSamlAttribute", "value");
        
        final List<Authentication> authentications = new ArrayList<Authentication>();
        authentications.add(authentication);
        
        final Assertion assertion = new ImmutableAssertionImpl(authentications, TestUtils.getService(), true);
        
        model.put("assertion", assertion);
        
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        
        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();
        
        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains(SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_SSL_TLS_CLIENT));
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
        
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        
        this.response.renderMergedOutputModel(model, new MockHttpServletRequest(), servletResponse);
        final String written = servletResponse.getContentAsString();
        
        assertTrue(written.contains("testPrincipal"));
        assertTrue(written.contains("testAttribute"));
        assertTrue(written.contains("testValue"));
        assertTrue(written.contains("urn:oasis:names:tc:SAML:1.0:am:unspecified"));       
    }
}
