package org.apereo.cas.config;

import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link CasRegisteredServicesTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration("casRegisteredServicesTestConfiguration")
@Lazy(false)
public class CasRegisteredServicesTestConfiguration {

    @Bean
    public RegisteredServicePrincipalAttributesRepository cachingPrincipalAttributeRepository() {
        return new CachingPrincipalAttributesRepository("SECONDS", 20);
    }

    @ConditionalOnMissingBean(name = "inMemoryRegisteredServices")
    @Bean
    @SneakyThrows
    public List inMemoryRegisteredServices() {
        val l = new ArrayList<>();

        val svc = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);
        val publicKey = new RegisteredServicePublicKeyImpl();
        publicKey.setLocation("classpath:keys/RSA1024Public.key");
        svc.setPublicKey(publicKey);
        svc.setAttributeReleasePolicy(policy);
        l.add(svc);

        val svc2 = RegisteredServiceTestUtils.getRegisteredService("testDefault");
        svc2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc2.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/normal/.*");
        svc3.setEvaluationOrder(10);
        svc3.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc3.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc3);

        val svc4 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/high/.*");
        svc4.setEvaluationOrder(20);
        svc4.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val handlers = CollectionUtils.wrapHashSet(AcceptUsersAuthenticationHandler.class.getSimpleName(), TestOneTimePasswordAuthenticationHandler.class.getSimpleName());
        svc4.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(handlers);
        svc4.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc4);

        val svc5 = RegisteredServiceTestUtils.getRegisteredService("(https://)*google.com$");
        svc5.setEvaluationOrder(1);
        svc5.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc5.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        val policy1 = new ReturnAllowedAttributeReleasePolicy();
        policy1.setAuthorizedToReleaseCredentialPassword(true);
        policy1.setAuthorizedToReleaseProxyGrantingTicket(true);
        policy1.setAllowedAttributes(CollectionUtils.wrap("binaryAttribute"));
        svc5.setAttributeReleasePolicy(policy1);
        svc5.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc5.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc5);

        val svc6 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTest");
        svc6.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("eduPersonAffiliation"));
        svc6.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc6.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc6);

        val svc7 = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        val policy2 = new ReturnAllowedAttributeReleasePolicy();
        policy2.setAuthorizedToReleaseCredentialPassword(true);
        policy2.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc7.setAttributeReleasePolicy(policy2);
        svc7.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA"));
        svc7.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc7.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc7);

        val svc8 = RegisteredServiceTestUtils.getRegisteredService("^TestServiceAttributeForAuthzFails");
        svc8.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("cn", CollectionUtils.wrapSet("cnValue"),
            "givenName", CollectionUtils.wrapSet("gnameValue"))));
        svc8.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc8);

        val svc9 = RegisteredServiceTestUtils.getRegisteredService("^TestSsoFalse");
        svc9.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        svc9.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc9);

        val svc10 = RegisteredServiceTestUtils.getRegisteredService("TestServiceAttributeForAuthzPasses");
        svc10.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("groupMembership", CollectionUtils.wrapSet("adopters"))));
        svc10.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc10.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc10);

        val svc11 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTestInvalid");
        svc11.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("nonExistentAttributeName"));
        svc11.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrap("groupMembership")));
        svc11.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc11);

        val svc12 = RegisteredServiceTestUtils.getRegisteredService("testAnonymous");
        svc12.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        svc12.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc12);

        val svc13 = RegisteredServiceTestUtils.getRegisteredService("^http://www.jasig.org.+");
        svc13.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc13.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc13.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc13);

        val svc14 = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        svc14.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        svc14.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc14);

        val svc15 = RegisteredServiceTestUtils.getRegisteredService("proxyService");
        svc15.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https://.+"));
        svc15.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc15.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc15);

        val svc16 = RegisteredServiceTestUtils.getRegisteredService("^test.*");
        svc16.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc16.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc16.setEvaluationOrder(1000);
        l.add(svc16);

        val svc17 = RegisteredServiceTestUtils.getRegisteredService("https://localhost.*");
        svc17.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc17.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc17.setEvaluationOrder(100);
        l.add(svc17);

        val svc18 = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        svc18.setAccessStrategy(accessStrategy);
        svc18.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc18.setEvaluationOrder(98);
        l.add(svc18);

        val svc19 = RegisteredServiceTestUtils.getRegisteredService("https://carmenwiki.osu.edu.*");
        svc19.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc19.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc19.setEvaluationOrder(99);
        l.add(svc19);

        val svc20 = RegisteredServiceTestUtils.getRegisteredService("consentService");
        svc20.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc20.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        svc20.setAttributeReleasePolicy(attrPolicy);
        svc20.setEvaluationOrder(88);
        l.add(svc20);

        val svc21 = RegisteredServiceTestUtils.getRegisteredService("jwtservice");
        svc21.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc21.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val prop = new DefaultRegisteredServiceProperty();
        prop.setValues(CollectionUtils.wrapSet(Boolean.TRUE.toString()));
        svc21.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName(), prop);
        svc21.setEvaluationOrder(2000);
        l.add(svc21);

        val svc22 = RegisteredServiceTestUtils.getRegisteredService("cas-access-disabled");
        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setEnabled(false);
        strategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        svc22.setAccessStrategy(strategy);
        l.add(svc22);

        val svc23 = RegisteredServiceTestUtils.getRegisteredService("cas-access-delegation");
        val strategy23 = new DefaultRegisteredServiceAccessStrategy();
        strategy23.setEnabled(true);
        val delegate = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        delegate.setExclusive(true);
        strategy23.setDelegatedAuthenticationPolicy(delegate);
        svc23.setAccessStrategy(strategy23);
        l.add(svc23);

        val svc24 = RegisteredServiceTestUtils.getRegisteredService("https://www.casinthecloud.com");
        svc24.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc24.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        val policy24 = new ReturnAllowedAttributeReleasePolicy();
        policy24.setAuthorizedToReleaseCredentialPassword(true);
        policy24.setAuthorizedToReleaseProxyGrantingTicket(false);
        policy24.setAllowedAttributes(CollectionUtils.wrap("binaryAttribute"));
        svc24.setAttributeReleasePolicy(policy24);
        svc24.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc24.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc24);

        return l;
    }
}
