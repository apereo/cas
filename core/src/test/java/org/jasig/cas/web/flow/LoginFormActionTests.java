/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
// TODO re-enable tests
public class LoginFormActionTests extends
    AbstractCentralAuthenticationServiceTest {

    private LoginFormAction loginFormAction;

    protected void onSetUp() throws Exception {
        this.loginFormAction = new LoginFormAction();
        this.loginFormAction
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.loginFormAction.afterPropertiesSet();
    }

    public void testSubmitBadCredentials() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request,
            new MockHttpServletResponse()));

        ContextUtils.addAttribute(context, "credentials", TestUtils
            .getCredentialsWithDifferentUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword(),
                "credentials"));

        assertEquals("error", this.loginFormAction.submit(context).getId());
    }

    public void testSubmitProperCredentialsWithService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("service", "test");

        assertEquals("warn", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request)).getId());
    }

    public void testSubmitProperCredentialsWithNoService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("noService", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request)).getId());
    }

    public void testSetCookieValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        this.loginFormAction.setCookieTimeout(5);

        assertEquals("noService", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request, response)).getId());
        assertEquals(5, response.getCookies()[0].getMaxAge());
    }

    public void testWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("warn", "on");

        assertEquals("noService", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request, response)).getId());
        assertNotNull(response.getCookie(WebConstants.COOKIE_PRIVACY));
        assertEquals(WebConstants.COOKIE_DEFAULT_FILLED_VALUE, response
            .getCookie(WebConstants.COOKIE_PRIVACY).getValue());
    }

    public void testRenewIsTrue() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("service", "true");
        request.addParameter("renew", "true");

        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        request.setCookies(new Cookie[] {new Cookie(WebConstants.COOKIE_TGC_ID,
            ticketGrantingTicket)});

        assertEquals("warn", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request)).getId());
    }

    public void testRenewIsTrueWithDifferentCredentials() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter("service", "true");
        request.addParameter("renew", "true");

        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword("test2"));
        request.setCookies(new Cookie[] {new Cookie(WebConstants.COOKIE_TGC_ID,
            ticketGrantingTicket)});

        assertEquals("warn", this.loginFormAction.submit(
            TestUtils.getContextWithCredentials(request)).getId());
    }

    public void testAfterPropertiesSetCas() {
        try {
            this.loginFormAction.setCentralAuthenticationService(null);
            this.loginFormAction.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetBadCredentials() {
        try {
            this.loginFormAction.setFormObjectClass(Object.class);
            this.loginFormAction.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetDifferentCredentials() {
        try {
            this.loginFormAction
                .setFormObjectClass(HttpBasedServiceCredentials.class);
            this.loginFormAction.setValidator(new Validator(){

                public boolean supports(Class arg0) {
                    return true;
                }

                public void validate(Object arg0, Errors arg1) {
                    // do nothing
                }
            });
            this.loginFormAction.setCredentialsBinder(new CredentialsBinder(){

                public void bind(HttpServletRequest request,
                    Credentials credentials) {
                    // do nothing
                }

                public boolean supports(Class clazz) {
                    return false;
                }
            });
            this.loginFormAction.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

/*    public void testOnBindNoBinding() throws IllegalAccessException,
        InvocationTargetException {
        this.loginFormAction
            .setFormObjectClass(UsernamePasswordCredentials.class);
        this.loginFormAction.setCredentialsBinder(null);
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request,
            new MockHttpServletResponse()));
        Method[] methods = this.loginFormAction.getClass().getDeclaredMethods();
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        Method method = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("onBind")
                && methods[i].getParameterTypes().length == 3) {
                method = methods[i];
                break;
            }
        }

        method.invoke(this.loginFormAction, new Object[] {context, c,
            new BindException(c, "credentials")});
    }*/

    /*
    public void testBinding() throws IllegalAccessException,
        InvocationTargetException {
        this.loginFormAction
            .setFormObjectClass(UsernamePasswordCredentials.class);
        this.loginFormAction.setCredentialsBinder(new CredentialsBinder(){

            public void bind(HttpServletRequest request, Credentials credentials) {
                UsernamePasswordCredentials c = (UsernamePasswordCredentials) credentials;
                c.setUsername("test");
            }

            public boolean supports(Class clazz) {
                return true;
            }
        });
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request,
            new MockHttpServletResponse()));
        Method[] methods = this.loginFormAction.getClass().getDeclaredMethods();
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        Method method = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("onBind")
                && methods[i].getParameterTypes().length == 3) {
                method = methods[i];
                break;
            }
        }

        method.invoke(this.loginFormAction, new Object[] {context, c,
            new BindException(c, "credentials")});
        assertEquals("test", c.getUsername());
    }
    */
}
