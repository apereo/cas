package org.jasig.cas.services;

import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TestUtils {

    public static final String CONST_TEST_URL = "https://google.com";
    public static final String CONST_TEST_URL2 = "https://example.com";

    private TestUtils() {}

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_TEST_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(
            final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url),
                    TestUtils.getRegisteredService(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(
            final String username, final String password) {
        final UsernamePasswordCredential usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }
    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static Service getService2() {
        return getService(CONST_TEST_URL2);
    }

    public static AbstractWebApplicationService getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        final AbstractWebApplicationService result = (AbstractWebApplicationService)
                new WebApplicationServiceFactory().createService(request);
        return result;
    }

    public static Map<String, Set<String>> getTestAttributes() {
        final Map<String, Set<String>>  attributes = new HashMap<>();
        Set<String> attributeValues = new HashSet<>();
        attributeValues.add("uid");

        attributes.put("uid", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add("CASUser");

        attributes.put("givenName", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add("admin");
        attributeValues.add("system");
        attributeValues.add("cas");

        attributes.put("memberOf", attributeValues);
        return attributes;
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
                    new CachingPrincipalAttributesRepository(TimeUnit.SECONDS, 10);
            repo.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
            policy.setPrincipalAttributesRepository(repo);
            policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
            policy.setAllowedAttributes(new ArrayList(getTestAttributes().keySet()));
            s.setAttributeReleasePolicy(policy);

            return s;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
