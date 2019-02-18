package org.apereo.cas.services;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceAuthenticationHandlerResolverTests {

    private DefaultServicesManager defaultServicesManager;
    private Set<AuthenticationHandler> authenticationHandlers;

    @BeforeEach
    public void initialize() {

        val list = new ArrayList<RegisteredService>();

        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        svc.setRequiredHandlers(CollectionUtils.wrapHashSet("handler1", "handler2"));
        list.add(svc);

        svc = RegisteredServiceTestUtils.getRegisteredService("serviceid2");
        svc.setRequiredHandlers(new HashSet<>(0));
        list.add(svc);

        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), list);

        this.defaultServicesManager = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class), new HashSet<>());
        this.defaultServicesManager.load();

        val handler1 = new AcceptUsersAuthenticationHandler("handler1");
        val handler2 = new AcceptUsersAuthenticationHandler("handler2");
        val handler3 = new AcceptUsersAuthenticationHandler("handler3");

        this.authenticationHandlers = Stream.of(handler1, handler2, handler3).collect(Collectors.toSet());
    }

    @Test
    public void checkAuthenticationHandlerResolutionDefault() {
        val resolver = new RegisteredServiceAuthenticationHandlerResolver(this.defaultServicesManager);
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("serviceid1"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(2, handlers.size());
    }

    @Test
    public void checkAuthenticationHandlerResolution() {
        val resolver = new DefaultAuthenticationHandlerResolver();
        val transaction = DefaultAuthenticationTransaction.of(RegisteredServiceTestUtils.getService("serviceid2"),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val handlers = resolver.resolve(this.authenticationHandlers, transaction);
        assertEquals(handlers.size(), this.authenticationHandlers.size());
    }
}
