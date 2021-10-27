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
package org.jasig.cas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.CachingPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredServicePublicKeyImpl;
import org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ImmutableAssertion;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Scott Battaglia
 * @since 3.0.0.2
 */
public final class TestUtils {

    public static final String CONST_USERNAME = "test";

    public static final String CONST_TEST_URL = "https://test.com";

    public static final String CONST_EXCEPTION_EXPECTED = "Exception expected.";

    public static final String CONST_EXCEPTION_NON_EXPECTED = "Exception not expected.";

    public static final String CONST_GOOD_URL = "https://github.com/";

    private static final String CONST_PASSWORD = "test1";

    private static final String CONST_CREDENTIALS = "credentials";

    private static final String CONST_WEBFLOW_BIND_EXCEPTION =
            "org.springframework.validation.BindException.credentials";

    private static final String[] CONST_NO_PRINCIPALS = new String[0];

    private TestUtils() {
        // do not instantiate
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

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(
        final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url), TestUtils.getRegisteredService(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static IPersonAttributeDao getAttributeRepository() {
        final Map<String, List<Object>>  attributes = new HashMap<>();
        attributes.put("uid", (List) ImmutableList.of(CONST_USERNAME));
        attributes.put("cn", (List) ImmutableList.of(CONST_USERNAME.toUpperCase()));
        attributes.put("givenName", (List) ImmutableList.of(CONST_USERNAME));
        attributes.put("memberOf", (List) ImmutableList.of("system", "admin", "cas"));
        return new StubPersonAttributeDao(attributes);
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return getPrincipal(name, Collections.EMPTY_MAP);
    }

    public static Principal getPrincipal(final String name, final Map<String, Object> attributes) {
        return new DefaultPrincipalFactory().createPrincipal(name, attributes);
    }

    public static AbstractRegisteredService getRegisteredService(final String id) {
        try  {
            final RegexRegisteredService s = new RegexRegisteredService();
            s.setServiceId(id);
            s.setEvaluationOrder(1);
            s.setName("Test registered service");
            s.setDescription("Registered service description");
            s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
            s.setId(new SecureRandom().nextInt(Math.abs(s.hashCode())));
            s.setTheme("exampleTheme");
            s.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));
            final DefaultRegisteredServiceAccessStrategy accessStrategy =
                    new DefaultRegisteredServiceAccessStrategy(true, true);
            accessStrategy.setRequireAllAttributes(true);
            accessStrategy.setRequiredAttributes(getTestAttributes());
            s.setAccessStrategy(accessStrategy);
            s.setLogo(new URL("https://logo.example.org/logo.png"));
            s.setLogoutType(LogoutType.BACK_CHANNEL);
            s.setLogoutUrl(new URL("https://sys.example.org/logout.png"));
            s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^http.+"));

            s.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:pub.key", "RSA"));

            final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAuthorizedToReleaseCredentialPassword(true);
            policy.setAuthorizedToReleaseProxyGrantingTicket(true);

            final CachingPrincipalAttributesRepository repo =
                    new CachingPrincipalAttributesRepository(10);
            repo.setMergingStrategy(CachingPrincipalAttributesRepository.MergingStrategy.ADD);
            policy.setPrincipalAttributesRepository(repo);
            policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
            policy.setAllowedAttributes(new ArrayList(getTestAttributes().keySet()));
            s.setAttributeReleasePolicy(policy);

            return s;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static Service getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return SimpleWebApplicationServiceImpl.createServiceFrom(request);
    }

    public static Authentication getAuthentication() {
        return getAuthentication(CONST_USERNAME);
    }

    public static Authentication getAuthentication(final String name) {
        return getAuthentication(getPrincipal(name));
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, Collections.<String, Object>emptyMap());
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, Object> attributes) {
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        return new DefaultAuthenticationBuilder(principal)
                .addCredential(meta)
                .addSuccess("testHandler", new DefaultHandlerResult(handler, meta))
                .setAttributes(attributes)
                .build();
    }

    public static Authentication getAuthenticationWithService() {
        return getAuthentication(getService());
    }

    public static Assertion getAssertion(final boolean fromNewLogin) {
        return getAssertion(fromNewLogin, CONST_NO_PRINCIPALS);
    }

    public static Assertion getAssertion(final boolean fromNewLogin,
        final String[] extraPrincipals) {
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication());

        for (int i = 0; i < extraPrincipals.length; i++) {
            list.add(TestUtils.getAuthentication(extraPrincipals[i]));
        }
        return new ImmutableAssertion(TestUtils.getAuthentication(), list, TestUtils.getService(), fromNewLogin);
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

    public static Map<String, Set<String>> getTestAttributes() {
        final Map<String, Set<String>>  attributes = new HashMap<>();
        attributes.put("uid", ImmutableSet.of("uid"));
        attributes.put("givenName", ImmutableSet.of("CASUser"));
        attributes.put("memberOf", ImmutableSet.of("system", "admin", "cas"));
        return attributes;
    }
}
