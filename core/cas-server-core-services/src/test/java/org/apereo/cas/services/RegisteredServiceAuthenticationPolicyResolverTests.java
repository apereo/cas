package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicy;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RegisteredServiceAuthenticationPolicyResolver;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class RegisteredServiceAuthenticationPolicyResolverTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @BeforeEach
    void initialize() {
        val svc1 = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        val p1 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr1 = new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria();
        cr1.setTryAll(true);
        p1.setCriteria(cr1);
        svc1.setAuthenticationPolicy(p1);
        servicesManager.save(svc1);

        val svc2 = RegisteredServiceTestUtils.getRegisteredService("serviceid2");
        svc2.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy());
        servicesManager.save(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService("serviceid3");
        val p3 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr3 = new AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria();
        p3.setCriteria(cr3);
        svc3.setAuthenticationPolicy(p3);
        servicesManager.save(svc3);

        val svc4 = RegisteredServiceTestUtils.getRegisteredService("serviceid4");
        val p4 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr4 = new NotPreventedRegisteredServiceAuthenticationPolicyCriteria();
        p4.setCriteria(cr4);
        svc4.setAuthenticationPolicy(p4);
        servicesManager.save(svc4);

        val svc5 = RegisteredServiceTestUtils.getRegisteredService("serviceid5");
        val p5 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr5 = new GroovyRegisteredServiceAuthenticationPolicyCriteria();
        cr5.setScript("groovy { return Optional.empty() }");
        p5.setCriteria(cr5);
        svc5.setAuthenticationPolicy(p5);
        servicesManager.save(svc5);

        val svc6 = RegisteredServiceTestUtils.getRegisteredService("serviceid6");
        val p6 = new DefaultRegisteredServiceAuthenticationPolicy();
        val cr6 = new RestfulRegisteredServiceAuthenticationPolicyCriteria();
        cr6.setUrl("https://example.org");
        cr6.setBasicAuthPassword("uid");
        cr6.setBasicAuthUsername("password");
        p6.setCriteria(cr6);
        svc6.setAuthenticationPolicy(p6);
        servicesManager.save(svc6);
    }

    @Test
    void checkAnyPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid1"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertInstanceOf(AtLeastOneCredentialValidatedAuthenticationPolicy.class, policies.iterator().next());
    }

    @Test
    void checkAllPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid3"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertInstanceOf(AllAuthenticationHandlersSucceededAuthenticationPolicy.class, policies.iterator().next());
    }

    @Test
    void checkDefaultPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val service = RegisteredServiceTestUtils.getService("serviceid2");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(
            service, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        assertFalse(resolver.supports(transaction));
        val policies = resolver.resolve(transaction);
        assertTrue(policies.isEmpty());

        val authPolicy = servicesManager.findServiceBy(service).getAuthenticationPolicy();
        assertNotNull(authPolicy);
        assertNull(authPolicy.getCriteria());
    }

    @Test
    void checkNotPreventedPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid4"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertInstanceOf(NotPreventedAuthenticationPolicy.class, policies.iterator().next());
    }

    @Test
    void checkGroovyPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid5"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertInstanceOf(GroovyScriptAuthenticationPolicy.class, policies.iterator().next());
    }

    @Test
    void checkDisabledPolicy() {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("not-found-service"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        assertThrows(UnauthorizedSsoServiceException.class, () -> resolver.supports(transaction));
    }

    @Test
    void checkRestPolicy() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationPolicyResolver(this.servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid6"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val policies = resolver.resolve(transaction);
        assertEquals(1, policies.size());
        assertInstanceOf(RestfulAuthenticationPolicy.class, policies.iterator().next());
    }

}
