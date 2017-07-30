package org.apereo.cas.config;

import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
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
import java.util.Map;
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
    public List inMemoryRegisteredServices() {
        final List l = new ArrayList();
        
        AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("testencryption$");
        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAuthorizedToReleaseCredentialPassword(true);
        policy.setAuthorizedToReleaseProxyGrantingTicket(true);
        final RegisteredServicePublicKeyImpl publicKey = new RegisteredServicePublicKeyImpl();
        publicKey.setLocation("classpath:RSA1024Public.key");
        svc.setPublicKey(publicKey);
        svc.setAttributeReleasePolicy(policy);
        l.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("testDefault");
        svc.setPublicKey(null);
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
        return l;
    }

}
