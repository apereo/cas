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
package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.validation.constraints.NotNull;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0.4
 */
public class AuthenticationViaFormActionTests extends AbstractCentralAuthenticationServiceTest {

    private AuthenticationViaFormAction action;

    private CookieGenerator warnCookieGenerator;

    @Before
    public void onSetUp() throws Exception {
        this.action = new AuthenticationViaFormAction();

        this.warnCookieGenerator = new CookieGenerator();
        this.warnCookieGenerator.setCookieName("WARN");
        this.warnCookieGenerator.setCookieName("TGT");
        this.warnCookieGenerator.setCookieDomain("/");
        this.warnCookieGenerator.setCookiePath("/");

        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");
        request.addParameter("username", "test");
        request.addParameter("password", "test");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifySuccessfulAuthenticationWithNoServiceAndWarn()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, response));
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifySuccessfulAuthenticationWithServiceAndWarn()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");
        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");
        request.addParameter("service", "test");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,  response));
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    @Test
    public void verifyFailedAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));

        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        putCredentialInRequestScope(context, c);

        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(c, "credentials"));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("error", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndSameCredentials() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(c);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        request.addParameter("username", "test");
        request.addParameter("password", "test");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put("service", TestUtils.getService());

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("warn", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndDifferentCredentials() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(c);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putLoginTicket(context, "LOGIN");
        request.addParameter("lt", "LOGIN");

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        request.addParameter("username", "test2");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("success", this.action.submit(context, c, messageContext).getId());
    }

    @Test
    public void verifyRenewWithServiceAndBadCredentials() throws Exception {
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(c);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", "test");

        final Credential c2 = TestUtils.getCredentialsWithDifferentUsernameAndPassword();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        putCredentialInRequestScope(context, c2);
        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(c2, "credentials"));

        final MessageContext messageContext = mock(MessageContext.class);
        assertEquals("error", this.action.submit(context, c2, messageContext).getId());
    }


    /**
     * Put credentials in request scope.
     *
     * @param context the context
     * @param c the credential
     */
    private static void putCredentialInRequestScope(
            final RequestContext context, @NotNull final Credential c) {
        context.getRequestScope().put("credentials", c);
    }
}
