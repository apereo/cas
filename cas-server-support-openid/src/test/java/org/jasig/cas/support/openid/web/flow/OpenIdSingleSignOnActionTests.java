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
package org.jasig.cas.support.openid.web.flow;


import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.DirectMappingAuthenticationManagerImpl;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.DirectMappingAuthenticationManagerImpl.DirectAuthenticationHandlerMappingHolder;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentialsToPrincipalResolver;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.flow.OpenIdSingleSignOnAction;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdSingleSignOnActionTests extends TestCase {

    private OpenIdSingleSignOnAction action;
    
    private TicketRegistry ticketRegistry;
    
    private DirectMappingAuthenticationManagerImpl authenticationManager;
    
    private CentralAuthenticationServiceImpl impl = new CentralAuthenticationServiceImpl();
    
    protected void setUp() throws Exception {
        this.ticketRegistry = new DefaultTicketRegistry();
        this.authenticationManager = new DirectMappingAuthenticationManagerImpl();
        
        final Map<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder> credentialsMapping = new HashMap<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder>();
        
        final DirectAuthenticationHandlerMappingHolder holder = new DirectAuthenticationHandlerMappingHolder();
        final OpenIdCredentialsAuthenticationHandler handler = new OpenIdCredentialsAuthenticationHandler();
        handler.setTicketRegistry(this.ticketRegistry);
        holder.setAuthenticationHandler(handler);
        holder.setCredentialsToPrincipalResolver(new OpenIdCredentialsToPrincipalResolver());
        
        this.authenticationManager.setCredentialsMapping(credentialsMapping);
        credentialsMapping.put(OpenIdCredentials.class, holder);
        
        final Map<String, UniqueTicketIdGenerator> generator = new HashMap<String, UniqueTicketIdGenerator>();
        generator.put(OpenIdService.class.getName(), new DefaultUniqueTicketIdGenerator());
        
        this.impl.setAuthenticationManager(this.authenticationManager);
        this.impl.setServicesManager(new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl()));
        this.impl.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.impl.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.impl.setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        this.impl.setTicketRegistry(this.ticketRegistry);
        this.impl.setUniqueTicketIdGeneratorsForService(generator);

        this.action = new OpenIdSingleSignOnAction();
        this.action.setCentralAuthenticationService(this.impl);
        this.action.setExtractor(new DefaultOpenIdUserNameExtractor());
        this.action.afterPropertiesSet();
    }
    
    public void testNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        Event event = this.action.execute(context);
        
        assertNotNull(event);
        
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testBadUsername() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("openid.identity", "fablah");
        request.setParameter("openid.return_to", "http://www.cnn.com");
        
        final OpenIdService service = OpenIdService.createServiceFrom(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", "tgtId");
        
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testSuccessfulServiceTicket() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authentication = new MutableAuthentication(new SimplePrincipal("scootman28"));
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("TGT-11", authentication, new NeverExpiresExpirationPolicy());
        
        this.ticketRegistry.addTicket(t);
        
        request.setParameter("openid.identity", "http://openid.aol.com/scootman28");
        request.setParameter("openid.return_to", "http://www.cnn.com");

        final OpenIdService service = OpenIdService.createServiceFrom(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", t.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}
