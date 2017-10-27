package org.apereo.cas.config;

import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.AbstractRegisteredService;
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
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CasRegisteredServicesTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration("casRegisteredServicesTestConfiguration")
public class CasRegisteredServicesTestConfiguration {

    @Bean
    public PrincipalAttributesRepository cachingPrincipalAttributeRepository() {
        return new CachingPrincipalAttributesRepository("SECONDS", 20);
    }

    @Bean
    public List inMemoryRegisteredServices() {
        final List l = new ArrayList();

        AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);
        final RegisteredServicePublicKeyImpl publicKey = new RegisteredServicePublicKeyImpl();
        publicKey.setLocation("classpath:keys/RSA1024Public.key");
        svc.setPublicKey(publicKey);
        svc.setAttributeReleasePolicy(policy);
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("testDefault");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/normal/.*");
        svc.setEvaluationOrder(10);
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("https://example\\.com/high/.*");
        svc.setEvaluationOrder(20);
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Set handlers = CollectionUtils.wrapSet(AcceptUsersAuthenticationHandler.class.getSimpleName(),
                TestOneTimePasswordAuthenticationHandler.class.getSimpleName());
        svc.setRequiredHandlers(handlers);
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);


        svc = RegisteredServiceTestUtils.getRegisteredService("(https://)*google.com$");
        svc.setEvaluationOrder(1);
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA4096Public.key", "RSA"));
        final ReturnAllowedAttributeReleasePolicy policy1 = new ReturnAllowedAttributeReleasePolicy();
        policy1.setAuthorizedToReleaseCredentialPassword(true);
        policy1.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc.setAttributeReleasePolicy(policy1);
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("eduPersonTest");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("eduPersonAffiliation"));
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        final ReturnAllowedAttributeReleasePolicy policy2 = new ReturnAllowedAttributeReleasePolicy();
        policy2.setAuthorizedToReleaseCredentialPassword(true);
        policy2.setAuthorizedToReleaseProxyGrantingTicket(true);
        svc.setAttributeReleasePolicy(policy2);
        svc.setPublicKey(new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);


        svc = RegisteredServiceTestUtils.getRegisteredService("^TestServiceAttributeForAuthzFails");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("cn", CollectionUtils.wrapSet("cnValue"),
                "givenName", CollectionUtils.wrapSet("gnameValue"))));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("^TestSsoFalse");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("TestServiceAttributeForAuthzPasses");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("groupMembership", CollectionUtils.wrapSet("adopters"))));
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("eduPersonTestInvalid");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("nonExistentAttributeName"));
        svc.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(CollectionUtils.wrap("groupMembership")));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);


        svc = RegisteredServiceTestUtils.getRegisteredService("testAnonymous");
        svc.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("^http://www.jasig.org.+");
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy(".+"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        svc.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("proxyService");
        svc.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https://.+"));
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("^test.*");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        svc.setEvaluationOrder(1000);
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("jwtservice");
        svc.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(new HashMap<>()));
        svc.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final DefaultRegisteredServiceProperty prop = new DefaultRegisteredServiceProperty();
        prop.setValues(CollectionUtils.wrapSet(Boolean.TRUE.toString()));
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.getPropertyName(), prop);
        svc.setEvaluationOrder(2000);
        l.add(svc);
        return l;
    }

}
