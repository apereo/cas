/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class AuthenticationViaFormActionTests extends
    AbstractCentralAuthenticationServiceTest {

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

        this.action
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
 //       this.action.afterPropertiesSet();
    }

    @Test
    public void testSuccessfulAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        context.getRequestScope().put("credentials",
            TestUtils.getCredentialsWithSameUsernameAndPassword());
//        this.action.bind(context);
//        assertEquals("success", this.action.submit(context).getId());
    }

    @Test
    public void testSuccessfulAuthenticationWithNoServiceAndWarn()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, response));
        context.getRequestScope().put("credentials",
            TestUtils.getCredentialsWithSameUsernameAndPassword());
  //      this.action.bind(context);
   //     assertEquals("success", this.action.submit(context).getId());
//        assertNotNull(response.getCookie(this.warnCookieGenerator
//            .getCookieName()));
    }

    @Test
    public void testSuccessfulAuthenticationWithServiceAndWarn()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");
        request.addParameter("service", "test");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, response));
        context.getRequestScope().put("credentials",
            TestUtils.getCredentialsWithSameUsernameAndPassword());
 //       this.action.bind(context);
 //       assertEquals("success", this.action.submit(context).getId());
//        assertNotNull(response.getCookie(this.warnCookieGenerator
//            .getCookieName()));
    }

    @Test
    public void testFailedAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        request.addParameter("username", "test");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        context.getRequestScope().put("credentials",
            TestUtils.getCredentialsWithDifferentUsernameAndPassword());
        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword(),
                "credentials"));

    //    this.action.bind(context);
//        assertEquals("error", this.action.submit(context).getId());
    }

    @Test
    public void testRenewWithServiceAndSameCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        context.getFlowScope().put("ticketGrantingTicketId", ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        request.addParameter("username", "test");
        request.addParameter("password", "test");
        
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put("service", TestUtils.getService("test"));
    //    this.action.bind(context);
     //   assertEquals("warn", this.action.submit(context).getId());
    }

    @Test
    public void testRenewWithServiceAndDifferentCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        context.getFlowScope().put("ticketGrantingTicketId", ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        request.addParameter("username", "test2");
        request.addParameter("password", "test2");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
    //    this.action.bind(context);

    //    assertEquals("success", this.action.submit(context).getId());
    }

    @Test
    public void testRenewWithServiceAndBadCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();

        context.getFlowScope().put("ticketGrantingTicketId", ticketGrantingTicket);
        request.addParameter("renew", "true");
        request.addParameter("service", "test");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        context.getRequestScope().put("credentials",
            TestUtils.getCredentialsWithDifferentUsernameAndPassword());
        context.getRequestScope().put(
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword(),
                "credentials"));
   //     this.action.bind(context);
   //     assertEquals("error", this.action.submit(context).getId());
    }

    @Test
    public void testTestBindingWithoutCredentialsBinder() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
    //    context.setLastEvent(new Event(this, "test"));
        request.addParameter("username", "test");
        request.addParameter("password", "test");

   //     this.action.bind(context);
   //     assertEquals("success", this.action.bindAndValidate(context).getId());

    }

    @Test
    public void testTestBindingWithCredentialsBinder() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), new MockHttpServletRequest(),
            new MockHttpServletResponse()));
  //      context.setLastEvent(new Event(this, "test"));

        final CredentialsBinder cb = new CredentialsBinder(){

            public void bind(HttpServletRequest request, Credentials credentials) {
                ((UsernamePasswordCredentials) credentials)
                    .setUsername("test2");
                ((UsernamePasswordCredentials) credentials)
                    .setPassword("test2");
            }

            public boolean supports(Class<?> clazz) {
                return true;
            }

        };
        this.action.setCredentialsBinder(cb);
   //     this.action.bindAndValidate(context);

 //       assertEquals(
 //           "test2",
 //           ((UsernamePasswordCredentials) context
 //               .getFlowScope().get(
 //                   "credentials")).getUsername());

    }

    @Test
    public void testSetCredentialsBinderNoFailure() throws Exception {
        final CredentialsBinder c = new CredentialsBinder(){

            public void bind(final HttpServletRequest request,
                final Credentials credentials) {
                // nothing to do here
            }

            public boolean supports(final Class<?> clazz) {
                return true;
            }
        };

        this.action.setCredentialsBinder(c);
  //      this.action.afterPropertiesSet();
    }
}
