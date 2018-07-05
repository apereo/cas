package org.apereo.cas.config;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This is {@link CasRegisteredServicesTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration("casRegisteredServicesTestConfiguration")
@Slf4j
public class CasRegisteredServicesTestConfiguration {

    @Bean
    public PrincipalAttributesRepository cachingPrincipalAttributeRepository() {
        return new CachingPrincipalAttributesRepository("SECONDS", 20);
    }

    @Bean
    public List inMemoryRegisteredServices() {
        val l = new ArrayList();

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
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/normal/.*");
        svc.setEvaluationOrder(10);
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc3);

        val svc4 = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/high/.*");
        svc.setEvaluationOrder(20);
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final HashSet handlers = CollectionUtils.wrapHashSet(AcceptUsersAuthenticationHandler.class.getSimpleName(),
                TestOneTimePasswordAuthenticationHandler.class.getSimpleName());
        svc.setRequiredHandlers(handlers);
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc4);
        
        val svc5 = RegisteredServiceTestUtils.getRegisteredService("(https://)*google.com$");
        svc.setEvaluationOrder(1);
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        val policy1 = new ReturnAllowedAttributeReleasePolicy();
        policy1.setAuthorizedToReleaseCredentialPassword(true);
        policy1.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc.setAttributeReleasePolicy(policy1);
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc5);

        val svc6 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTest");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("eduPersonAffiliation"));
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc6);

        val svc7 = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        val policy2 = new ReturnAllowedAttributeReleasePolicy();
        policy2.setAuthorizedToReleaseCredentialPassword(true);
        policy2.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc.setAttributeReleasePolicy(policy2);
        svc.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc7);
        
        val svc8 = RegisteredServiceTestUtils.getRegisteredService("^TestServiceAttributeForAuthzFails");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("cn", CollectionUtils.wrapSet("cnValue"),
                "givenName", CollectionUtils.wrapSet("gnameValue"))));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc8);

        val svc9 = RegisteredServiceTestUtils.getRegisteredService("^TestSsoFalse");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc9);

        val svc10 = RegisteredServiceTestUtils.getRegisteredService("TestServiceAttributeForAuthzPasses");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("groupMembership", CollectionUtils.wrapSet("adopters"))));
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc10);

        val svc11 = RegisteredServiceTestUtils.getRegisteredService("eduPersonTestInvalid");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("nonExistentAttributeName"));
        svc.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrap("groupMembership")));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc11);


        val svc12 = RegisteredServiceTestUtils.getRegisteredService("testAnonymous");
        svc.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc12);

        val svc13 = RegisteredServiceTestUtils.getRegisteredService("^http://www.jasig.org.+");
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc13);

        val svc14 = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc14);

        val svc15 = RegisteredServiceTestUtils.getRegisteredService("proxyService");
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https://.+"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc15);

        val svc16 = RegisteredServiceTestUtils.getRegisteredService("^test.*");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setEvaluationOrder(1000);
        l.add(svc16);

        val svc17 = RegisteredServiceTestUtils.getRegisteredService("https://localhost.*");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setEvaluationOrder(100);
        l.add(svc17);

        val svc18 = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setEvaluationOrder(98);
        l.add(svc18);

        val svc19 = RegisteredServiceTestUtils.getRegisteredService("https://carmenwiki.osu.edu.*");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setEvaluationOrder(99);
        l.add(svc19);

        val svc20 = RegisteredServiceTestUtils.getRegisteredService("consentService");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy());
        svc.setAttributeReleasePolicy(attrPolicy);
        svc.setEvaluationOrder(88);
        l.add(svc20);
        
        val svc21 = RegisteredServiceTestUtils.getRegisteredService("jwtservice");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val prop = new DefaultRegisteredServiceProperty();
        prop.setValues(CollectionUtils.wrapSet(Boolean.TRUE.toString()));
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName(), prop);
        svc.setEvaluationOrder(2000);
        l.add(svc21);
        return l;
    }

}
