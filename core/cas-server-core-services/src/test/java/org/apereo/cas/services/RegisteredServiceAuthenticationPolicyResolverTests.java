package org.apereo.cas.services;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RegisteredServiceAuthenticationPolicyResolver;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RegisteredServiceAuthenticationPolicyResolverTests {
    private ServicesManager servicesManager;

    @BeforeEach
    public void initialize() {

        val list = new ArrayList<RegisteredService>();

        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        val p1 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr1 = new DefaultRegisteredServiceAuthenticationPolicyCriteria();
        cr1.setType(RegisteredServiceAuthenticationPolicyCriteria.AuthenticationPolicyTypes.ANY_AUTHENTICATION_HANDLER);
        cr1.setTryAll(true);
        p1.setCriteria(cr1);
        svc.setAuthenticationPolicy(p1);
        list.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("serviceid2");
        svc.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy());
        list.add(svc);

        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), list, new ArrayList<>());

        this.servicesManager = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class), new HashSet<>());
        this.servicesManager.load();
    }

    @Test
    public void checkAnyCredsPolicy() {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("serviceid1"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertTrue(policies.iterator().next() instanceof AtLeastOneCredentialValidatedAuthenticationPolicy);
    }

    @Test
    public void checkDefaultPolicy() {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("serviceid2"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        assertFalse(resolver.supports(transaction));
        val policies = resolver.resolve(transaction);
        assertTrue(policies.isEmpty());
    }
}
