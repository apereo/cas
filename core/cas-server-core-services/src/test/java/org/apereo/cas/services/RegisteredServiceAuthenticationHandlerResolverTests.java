package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
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
 * This is {@link RegisteredServiceAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class RegisteredServiceAuthenticationHandlerResolverTests {

    private static final String SERVICE_ID_PREFIX = "handler-serviceid";

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    private Set<AuthenticationHandler> authenticationHandlers;

    @BeforeEach
    void initialize() {
        val svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID_PREFIX + '1');
        svc.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(CollectionUtils.wrapHashSet("handler1", "handler2"));
        servicesManager.save(svc);

        val svc2 = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID_PREFIX + '2');
        svc2.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(new HashSet<>());
        servicesManager.save(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID_PREFIX + '3');
        svc3.getAuthenticationPolicy().getExcludedAuthenticationHandlers().addAll(Set.of("handler3", "handler1"));
        servicesManager.save(svc3);
        
        val handler1 = new AcceptUsersAuthenticationHandler("handler1");
        val handler2 = new AcceptUsersAuthenticationHandler("handler2");
        val handler3 = new AcceptUsersAuthenticationHandler("handler3");

        this.authenticationHandlers = Stream.of(handler1, handler2, handler3).collect(Collectors.toSet());
    }

    @Test
    void checkAuthenticationHandlerResolutionDefault() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(
            RegisteredServiceTestUtils.getService(SERVICE_ID_PREFIX + '1'),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(2, handlers.size());
    }

    @Test
    void checkAuthenticationHandlerResolution() throws Throwable {
        val resolver = new DefaultAuthenticationHandlerResolver();
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService(SERVICE_ID_PREFIX + '2'),
                RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(Objects.requireNonNull(handlers).size(), this.authenticationHandlers.size());
    }

    @Test
    void checkAuthenticationHandlerExcluded() throws Throwable {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService(SERVICE_ID_PREFIX + '3'),
                RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(1, handlers.size());
        assertEquals("handler2", handlers.iterator().next().getName());
    }
}
