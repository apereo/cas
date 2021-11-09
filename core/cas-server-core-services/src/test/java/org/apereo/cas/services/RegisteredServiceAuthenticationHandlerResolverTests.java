package org.apereo.cas.services;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.util.CollectionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("RegisteredService")
public class RegisteredServiceAuthenticationHandlerResolverTests {

    private ServicesManager defaultServicesManager;

    private Set<AuthenticationHandler> authenticationHandlers;

    @BeforeEach
    public void initialize() {

        val list = new ArrayList<RegisteredService>();

        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        svc.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(CollectionUtils.wrapHashSet("handler1", "handler2"));
        list.add(svc);

        val svc2 = RegisteredServiceTestUtils.getRegisteredService("serviceid2");
        svc2.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(new HashSet<>(0));
        list.add(svc2);

        val svc3 = RegisteredServiceTestUtils.getRegisteredService("serviceid3");
        svc3.getAuthenticationPolicy().getExcludedAuthenticationHandlers().addAll(Set.of("handler3", "handler1"));
        list.add(svc3);

        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val dao = new InMemoryServiceRegistry(appCtx, list, new ArrayList<>());

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(dao)
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        defaultServicesManager = new DefaultServicesManager(context);
        defaultServicesManager.load();

        val handler1 = new AcceptUsersAuthenticationHandler("handler1");
        val handler2 = new AcceptUsersAuthenticationHandler("handler2");
        val handler3 = new AcceptUsersAuthenticationHandler("handler3");

        this.authenticationHandlers = Stream.of(handler1, handler2, handler3).collect(Collectors.toSet());
    }

    @Test
    public void checkAuthenticationHandlerResolutionDefault() {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(this.defaultServicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(RegisteredServiceTestUtils.getService("serviceid1"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(2, handlers.size());
    }

    @Test
    public void checkAuthenticationHandlerResolution() {
        val resolver = new DefaultAuthenticationHandlerResolver();
        val transaction = new DefaultAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService("serviceid2"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(handlers.size(), this.authenticationHandlers.size());
    }

    @Test
    public void checkAuthenticationHandlerExcluded() {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(this.defaultServicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        val transaction = new DefaultAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService("serviceid3"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(1, handlers.size());
        assertEquals("handler2", handlers.iterator().next().getName());
    }
}
