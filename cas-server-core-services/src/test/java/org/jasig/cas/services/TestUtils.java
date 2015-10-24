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

package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TestUtils {

    public static final String CONST_TEST_URL = "https://test.com";
    public static final String CONST_TEST_URL2 = "https://example.com";

    private TestUtils() {}

    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static Service getService2() {
        return getService(CONST_TEST_URL2);
    }

    public static Service getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return new WebApplicationServiceFactory().createService(request);
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
            accessStrategy.setRequiredAttributes(org.jasig.cas.util.TestUtils.getTestAttributes());
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
                    new CachingPrincipalAttributesRepository(TimeUnit.SECONDS, 10);
            repo.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
            policy.setPrincipalAttributesRepository(repo);
            policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
            policy.setAllowedAttributes(new ArrayList(org.jasig.cas.util.TestUtils.getTestAttributes().keySet()));
            s.setAttributeReleasePolicy(policy);

            return s;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
