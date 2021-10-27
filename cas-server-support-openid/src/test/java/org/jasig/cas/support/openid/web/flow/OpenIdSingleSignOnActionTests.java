/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.openid.web.flow;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.PolicyBasedAuthenticationManager;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.support.openid.OpenIdConstants;
import org.jasig.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.jasig.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdSingleSignOnActionTests {

    private OpenIdSingleSignOnAction action;

    private TicketRegistry ticketRegistry;

    private AuthenticationManager authenticationManager;

    private CentralAuthenticationServiceImpl impl;

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = new DefaultTicketRegistry();
        final OpenIdCredentialsAuthenticationHandler handler = new OpenIdCredentialsAuthenticationHandler();
        handler.setTicketRegistry(this.ticketRegistry);
        this.authenticationManager = new PolicyBasedAuthenticationManager(
                Collections.<AuthenticationHandler, PrincipalResolver>singletonMap(
                        handler,
                        new OpenIdPrincipalResolver()));

        final Map<String, UniqueTicketIdGenerator> generator = new HashMap<>();
        generator.put(OpenIdService.class.getName(), new DefaultUniqueTicketIdGenerator());

        impl = new CentralAuthenticationServiceImpl(this.ticketRegistry, this.authenticationManager,
                new DefaultUniqueTicketIdGenerator(), generator, new NeverExpiresExpirationPolicy(),
                new NeverExpiresExpirationPolicy(),
                new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl()), mock(LogoutManager.class));

        this.action = new OpenIdSingleSignOnAction();
        this.action.setCentralAuthenticationService(this.impl);
        this.action.setExtractor(new DefaultOpenIdUserNameExtractor());
        this.action.afterPropertiesSet();
    }

    @Test
    public void verifyNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        final Event event = this.action.execute(context);

        assertNotNull(event);

        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyBadUsername() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(OpenIdConstants.OPENID_IDENTITY, "fablah");
        request.setParameter(OpenIdConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdService service = OpenIdService.createServiceFrom(request, null);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", "tgtId");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulServiceTicket() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authentication = TestUtils.getAuthentication("scootman28");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("TGT-11", authentication,
                new NeverExpiresExpirationPolicy());

        this.ticketRegistry.addTicket(t);

        request.setParameter(OpenIdConstants.OPENID_IDENTITY, "http://openid.aol.com/scootman28");
        request.setParameter(OpenIdConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdService service = OpenIdService.createServiceFrom(request, null);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", t.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}
