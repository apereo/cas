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
package org.jasig.cas;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.Principal;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.service.HttpBasedServiceCredential;
import org.jasig.cas.authentication.service.Service;
import org.jasig.cas.authentication.service.SimpleWebApplicationServiceImpl;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertionImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.2
 */
public final class TestUtils {

    public static final String CONST_USERNAME = "test";

    private static final String CONST_PASSWORD = "test1";

    private static final String CONST_BAD_URL = "http://www.acs.rutgers.edu";

    private static final String CONST_CREDENTIALS = "credentials";

    private static final String CONST_WEBFLOW_BIND_EXCEPTION = "org.springframework.validation.BindException.credentials";

    private static final String[] CONST_NO_PRINCIPALS = new String[0];

    public static final String CONST_EXCEPTION_EXPECTED = "Exception expected.";

    public static final String CONST_EXCEPTION_NON_EXPECTED = "Exception not expected.";
    
    public static final String CONST_GOOD_URL = "https://github.com/";

    public static final AuthenticationHandler AUTHENTICATION_HANDLER = new AuthenticationHandler() {
        @Override
        public HandlerResult authenticate(Credential credential) throws GeneralSecurityException, IOException {
            return new HandlerResult(this);
        }

        @Override
        public boolean supports(Credential credential) {
            return credential instanceof UsernamePasswordCredential;
        }

        @Override
        public String getName() {
            return "TestUtils.AUTHENTICATION_HANDLER";
        }
    };

    private TestUtils() {
        // do not instanciate
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword() {
        return getCredentialsWithSameUsernameAndPassword(CONST_USERNAME);
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword(
        final String username) {
        return getCredentialsWithDifferentUsernameAndPassword(username,
            username);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword() {
        return getCredentialsWithDifferentUsernameAndPassword(CONST_USERNAME,
            CONST_PASSWORD);
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(
        final String username, final String password) {
        // noinspection LocalVariableOfConcreteClass
        final UsernamePasswordCredential usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_GOOD_URL);
    }

    public static HttpBasedServiceCredential getBadHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_BAD_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(
        final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return new SimplePrincipal(name);
    }

    public static Service getService() {
        return getService(CONST_USERNAME);
    }

    public static Service getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return SimpleWebApplicationServiceImpl.createServiceFrom(request);
    }

    public static Authentication getAuthentication() {
        return newAuthentication(getPrincipal());
    }

    public static Authentication getAuthenticationWithService() {
        return newAuthentication(getService());
    }

    public static Authentication getAuthentication(final String name) {
        return newAuthentication(getPrincipal(name));
    }

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
        final String[] extraPrincipals) {
        final List<Authentication> list = new ArrayList<Authentication>();
        list.add(TestUtils.getAuthentication());

        for (int i = 0; i < extraPrincipals.length; i++) {
            list.add(TestUtils.getAuthentication(extraPrincipals[i]));
        }
        return new ImmutableAssertionImpl(list, TestUtils.getService(),
            fromNewLogin);
    }

    public static MockRequestContext getContext() {
        return getContext(new MockHttpServletRequest());
    }

    public static MockRequestContext getContext(
        final MockHttpServletRequest request) {
        return getContext(request, new MockHttpServletResponse());
    }

    public static MockRequestContext getContext(
        final MockHttpServletRequest request,
        final MockHttpServletResponse response) {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
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
        context.getRequestScope().put(CONST_CREDENTIALS, TestUtils
            .getCredentialsWithSameUsernameAndPassword());
        context.getRequestScope().put(CONST_WEBFLOW_BIND_EXCEPTION,
                new BindException(TestUtils
                    .getCredentialsWithSameUsernameAndPassword(),
                    CONST_CREDENTIALS));

        return context;
    }

    public static Authentication newAuthentication(final Principal p) {
        final HandlerResult hr = new HandlerResult(AUTHENTICATION_HANDLER, p);
        return new ImmutableAuthentication(p, null, Collections.singletonMap(hr, p), null);
    }
}
