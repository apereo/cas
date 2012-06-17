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
package org.jasig.cas.adaptors.x509.web.flow;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.jasig.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentialsToSerialNumberPrincipalResolver;
import org.jasig.cas.adaptors.x509.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;


public class X509CertificateCredentialsNonInteractiveActionTests extends
    AbstractX509CertificateTests {
    
    private X509CertificateCredentialsNonInteractiveAction action;
    
    protected void setUp() throws Exception {
        this.action = new X509CertificateCredentialsNonInteractiveAction();
        final CentralAuthenticationServiceImpl centralAuthenticationService = new CentralAuthenticationServiceImpl();
        centralAuthenticationService.setTicketRegistry(new DefaultTicketRegistry());
        final Map<String, UniqueTicketIdGenerator> idGenerators = new HashMap<String, UniqueTicketIdGenerator>();
        idGenerators.put(SimpleWebApplicationServiceImpl.class.getName(), new DefaultUniqueTicketIdGenerator());


        final AuthenticationManagerImpl authenticationManager = new AuthenticationManagerImpl();

        final X509CredentialsAuthenticationHandler a = new X509CredentialsAuthenticationHandler();
        a.setTrustedIssuerDnPattern("CN=\\w+,DC=jasig,DC=org");
        
        authenticationManager.setAuthenticationHandlers(Arrays.asList(new AuthenticationHandler[] {a}));
        authenticationManager.setCredentialsToPrincipalResolvers(Arrays.asList(new CredentialsToPrincipalResolver[] {new X509CertificateCredentialsToSerialNumberPrincipalResolver()}));
        
        centralAuthenticationService.setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        centralAuthenticationService.setUniqueTicketIdGeneratorsForService(idGenerators);
        centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setAuthenticationManager(authenticationManager);
        
        this.action.setCentralAuthenticationService(centralAuthenticationService);
        this.action.afterPropertiesSet();
    }
    
    public void testNoCredentialsResultsInError() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testCredentialsResultsInSuccess() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("javax.servlet.request.X509Certificate", new X509Certificate[] {VALID_CERTIFICATE});
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}
