package org.apereo.cas.services;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link RegisteredServiceTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@UtilityClass
public class RegisteredServiceTestUtils {
    public static final String CONST_USERNAME = "test";
    public static final String CONST_TEST_URL = "https://google.com";
    public static final String CONST_TEST_URL2 = "https://example.com";
    public static final String CONST_TEST_URL3 = "https://another.example.com";

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials() {
        return getHttpBasedServiceCredentials(CONST_TEST_URL);
    }

    public static HttpBasedServiceCredential getHttpBasedServiceCredentials(final String url) {
        try {
            return new HttpBasedServiceCredential(new URL(url), RegisteredServiceTestUtils.getRegisteredService(url));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    public static UsernamePasswordCredential getCredentialsWithSameUsernameAndPassword(final String username) {
        val usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(username);

        return usernamePasswordCredentials;
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(final String username, final String password) {
        val usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static AbstractWebApplicationService getService() {
        return getService(CONST_TEST_URL);
    }

    public static AbstractWebApplicationService getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    public static Service getService2() {
        return getService(CONST_TEST_URL2);
    }

    public static Map<String, Set<String>> getTestAttributes() {
        return getTestAttributes("CASUser");
    }

    public static Map<String, Set<String>> getTestAttributes(final String username) {
        val attributes = new HashMap<String, Set<String>>();
        Set<String> attributeValues = new HashSet<>();
        attributeValues.add("uid");

        attributes.put("uid", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add(username);

        attributes.put("givenName", attributeValues);

        attributeValues = new HashSet<>();
        attributeValues.add("admin");
        attributeValues.add("system");
        attributeValues.add("cas");

        attributes.put("memberOf", attributeValues);
        return attributes;
    }

    public static AbstractRegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static AbstractRegisteredService getRegisteredService(final Map requiredAttributes) {
        return getRegisteredService(CONST_TEST_URL, requiredAttributes);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id,
                                                                 final Class<? extends RegisteredService> clazz,
                                                                 final boolean uniq) {
         return getRegisteredService(id, clazz, uniq, getTestAttributes());
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id,
                                                                 final Class<? extends RegisteredService> clazz,
                                                                 final boolean uniq,
                                                                 final Map requiredAttributes) {
        val s = (AbstractRegisteredService) clazz.getDeclaredConstructor().newInstance();
        s.setServiceId(id);
        s.setEvaluationOrder(1);
        if (uniq) {
            val uuid = Iterables.get(Splitter.on('-').split(UUID.randomUUID().toString()), 0);
            s.setName("TestService" + uuid);
        } else {
            s.setName(id);
        }
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.nextInt());
        s.setTheme("exampleTheme");
        s.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy(true, true);
        accessStrategy.setRequireAllAttributes(true);
        accessStrategy.setRequiredAttributes(requiredAttributes);
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        s.setAccessStrategy(accessStrategy);
        s.setLogo("https://logo.example.org/logo.png");
        s.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        s.setLogoutUrl("https://sys.example.org/logout.png");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^http.+"));

        s.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA"));

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);

        val repo = new CachingPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 10);
        repo.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
        policy.setPrincipalAttributesRepository(repo);
        policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
        policy.setAllowedAttributes(new ArrayList<>(getTestAttributes().keySet()));
        s.setAttributeReleasePolicy(policy);

        return s;
    }

    public static AbstractRegisteredService getRegisteredService(final String id, final Class<? extends RegisteredService> clazz) {
        return getRegisteredService(id, clazz, true);
    }

    public static AbstractRegisteredService getRegisteredService(final String id) {
        return getRegisteredService(id, RegexRegisteredService.class, true);
    }

    public static AbstractRegisteredService getRegisteredService(final String id, final boolean uniq) {
        return getRegisteredService(id, RegexRegisteredService.class, uniq);
    }

    public static AbstractRegisteredService getRegisteredService(final String id, final Map requiredAttributes) {
        return getRegisteredService(id, RegexRegisteredService.class, true, requiredAttributes);
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return getPrincipal(name, new HashMap<>(0));
    }

    public static Principal getPrincipal(final String name, final Map<String, List<Object>> attributes) {
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(name, attributes);
    }

    public static Authentication getAuthentication() {
        return getAuthentication(CONST_USERNAME);
    }

    public static Authentication getAuthentication(final String name) {
        return getAuthentication(getPrincipal(name));
    }

    public static Authentication getAuthentication(final Principal principal) {
        return getAuthentication(principal, new HashMap<>(0));
    }

    public static Authentication getAuthentication(final Principal principal, final Map<String, List<Object>> attributes) {
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        val meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(meta)
            .addSuccess("testHandler", new DefaultAuthenticationHandlerExecutionResult(handler, meta))
            .setAttributes(attributes)
            .build();
    }

}
