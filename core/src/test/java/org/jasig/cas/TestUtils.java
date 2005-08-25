/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.2
 *
 */
public final class TestUtils {

    public static UsernamePasswordCredentials getCredentialsWithSameUsernameAndPassword() {
        return getCredentialsWithSameUsernameAndPassword("test");
    }

    public static UsernamePasswordCredentials getCredentialsWithSameUsernameAndPassword(
        final String username) {
        return getCredentialsWithDifferentUsernameAndPassword(username,
            username);
    }

    public static UsernamePasswordCredentials getCredentialsWithDifferentUsernameAndPassword(
        final String username, final String password) {
        final UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static HttpBasedServiceCredentials getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials("https://www.acs.rutgers.edu");
    }

    public static HttpBasedServiceCredentials getBadHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials("http://www.acs.rutgers.edu");
    }

    public static HttpBasedServiceCredentials getHttpBasedServiceCredentials(
        final String url) {
        try {
            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(
                new URL(url));
            return c;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static Principal getPrincipal() {
        return getPrincipal("test");
    }

    public static Principal getPrincipal(final String name) {
        return new SimplePrincipal(name);
    }

    public static Service getService() {
        return getService("test");
    }

    public static Service getService(final String name) {
        return new SimpleService(name);
    }

    public static Authentication getAuthentication() {
        return new ImmutableAuthentication(getPrincipal());
    }

    public static Authentication getAuthenticationWithService() {
        return new ImmutableAuthentication(getService());
    }

    public static Authentication getAuthentication(final String name) {
        return new ImmutableAuthentication(getPrincipal(name));
    }

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, new String[0]);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
        final String[] extraPrincipals) {
        final List list = new ArrayList();
        list.add(TestUtils.getPrincipal());

        for (int i = 0; i < extraPrincipals.length; i++) {
            list.add(TestUtils.getPrincipal(extraPrincipals[i]));
        }
        return new ImmutableAssertionImpl(list, TestUtils.getService(),
            fromNewLogin);
    }
    
    public static MockRequestContext getContext() {
        return getContext(new MockHttpServletRequest());
    }
    
    public static MockRequestContext getContext(final MockHttpServletRequest request) {
        return getContext(request, new MockHttpServletResponse());
    }
    
    public static MockRequestContext getContext(final MockHttpServletRequest request, final MockHttpServletResponse response) {
        final MockRequestContext context = new MockRequestContext();
        context.setSourceEvent(new ServletEvent(request, response));
        return context;
    }
    
    public static MockRequestContext getContextWithCredentials(
        final MockHttpServletRequest request) {
        return getContextWithCredentials(request, new MockHttpServletResponse());
    }

    public static MockRequestContext getContextWithCredentials(
        final MockHttpServletRequest request,
        final MockHttpServletResponse response) {
        final MockRequestContext context = getContext(request, response);
        ContextUtils.addAttribute(context, "credentials", TestUtils
            .getCredentialsWithSameUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithSameUsernameAndPassword(), "credentials"));

        return context;
    }
}
