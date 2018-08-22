package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.HttpBasedServiceCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredService.LogoutType;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.RandomUtils;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
@Slf4j
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
        final UsernamePasswordCredential usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(username);

        return usernamePasswordCredentials;
    }

    public static UsernamePasswordCredential getCredentialsWithDifferentUsernameAndPassword(final String username, final String password) {
        final UsernamePasswordCredential usernamePasswordCredentials = new UsernamePasswordCredential();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(password);

        return usernamePasswordCredentials;
    }

    public static Service getService() {
        return getService(CONST_TEST_URL);
    }

    public static AbstractWebApplicationService getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    public static Service getService2() {
        return getService(CONST_TEST_URL2);
    }

    public static Map<String, Set<String>> getTestAttributes() {
        final Map<String, Set<String>> attributes = new HashMap<>();
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

    public static AbstractRegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id, final Class<? extends RegisteredService> clazz) {
        final AbstractRegisteredService s = (AbstractRegisteredService) clazz.getDeclaredConstructor().newInstance();
        s.setServiceId(id);
        s.setEvaluationOrder(1);
        s.setName("TestService" + UUID.randomUUID().toString());
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getNativeInstance().nextInt(Math.abs(s.hashCode())));
        s.setTheme("exampleTheme");
        s.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));
        final DefaultRegisteredServiceAccessStrategy accessStrategy =
            new DefaultRegisteredServiceAccessStrategy(true, true);
        accessStrategy.setRequireAllAttributes(true);
        accessStrategy.setRequiredAttributes(getTestAttributes());
        s.setAccessStrategy(accessStrategy);
        s.setLogo("https://logo.example.org/logo.png");
        s.setLogoutType(LogoutType.BACK_CHANNEL);
        s.setLogoutUrl(new URL("https://sys.example.org/logout.png"));
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^http.+"));

        s.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA"));

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);

        final CachingPrincipalAttributesRepository repo =
            new CachingPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 10);
        repo.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
        policy.setPrincipalAttributesRepository(repo);
        policy.setAttributeFilter(new RegisteredServiceRegexAttributeFilter("https://.+"));
        policy.setAllowedAttributes(new ArrayList<>(getTestAttributes().keySet()));
        s.setAttributeReleasePolicy(policy);

        return s;
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id) {
        return getRegisteredService(id, RegexRegisteredService.class);
    }

    public static Principal getPrincipal() {
        return getPrincipal(CONST_USERNAME);
    }

    public static Principal getPrincipal(final String name) {
        return getPrincipal(name, new HashMap<>(0));
    }

    public static Principal getPrincipal(final String name, final Map<String, Object> attributes) {
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

    public static Authentication getAuthentication(final Principal principal, final Map<String, Object> attributes) {
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        return new DefaultAuthenticationBuilder(principal)
            .addCredential(meta)
            .addSuccess("testHandler", new DefaultAuthenticationHandlerExecutionResult(handler, meta))
            .setAttributes(attributes)
            .build();
    }

}
