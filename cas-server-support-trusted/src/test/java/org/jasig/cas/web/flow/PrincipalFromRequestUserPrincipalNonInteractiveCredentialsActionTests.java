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
package org.jasig.cas.web.flow;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.PrincipalBearingCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.PrincipalBearingPrincipalResolver;
import org.jasig.cas.authentication.AnyAuthenticationManger;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.PrincipalResolver;
import org.jasig.cas.authentication.service.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests
    extends TestCase {

    private PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction action;
    
    protected void setUp() throws Exception {
        this.action = new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction();
        
        final CentralAuthenticationServiceImpl centralAuthenticationService = new CentralAuthenticationServiceImpl();
        centralAuthenticationService.setTicketRegistry(new DefaultTicketRegistry());
        final Map<String, UniqueTicketIdGenerator> idGenerators = new HashMap<String, UniqueTicketIdGenerator>();
        idGenerators.put(SimpleWebApplicationServiceImpl.class.getName(), new DefaultUniqueTicketIdGenerator());

        final AuthenticationManager authenticationManager = new AnyAuthenticationManger(
                Collections.<AuthenticationHandler, PrincipalResolver>singletonMap(
                        new PrincipalBearingCredentialsAuthenticationHandler(),
                        new PrincipalBearingPrincipalResolver()));
        
        centralAuthenticationService.setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        centralAuthenticationService.setUniqueTicketIdGeneratorsForService(idGenerators);
        centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setAuthenticationManager(authenticationManager);
        
        this.action.setCentralAuthenticationService(centralAuthenticationService);
    }
    
    public void testRemoteUserExists() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new Principal() {public String getName() {return "test";}});
        
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        assertEquals("success", this.action.execute(context).getId());
    }
    
    public void testRemoteUserDoesntExists() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        
        assertEquals("error", this.action.execute(context).getId());
    }
    
}
