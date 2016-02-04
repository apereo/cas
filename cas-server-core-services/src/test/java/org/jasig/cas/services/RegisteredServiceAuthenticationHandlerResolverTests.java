package org.jasig.cas.services;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.AcceptUsersAuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.jasig.cas.util.ServicesTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class RegisteredServiceAuthenticationHandlerResolverTests {

    private DefaultServicesManagerImpl defaultServicesManagerImpl;
    private Set<AuthenticationHandler> handlers;

    @Before
    public void setUp() throws Exception {
        final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
        final List<RegisteredService> list = new ArrayList<>();

        AbstractRegisteredService svc = ServicesTestUtils.getRegisteredService("serviceid1");
        svc.setRequiredHandlers(ImmutableSet.of("handler1", "handler3"));
        list.add(svc);

        svc = ServicesTestUtils.getRegisteredService("serviceid2");
        svc.setRequiredHandlers(Collections.EMPTY_SET);
        list.add(svc);

        dao.setRegisteredServices(list);
        this.defaultServicesManagerImpl = new DefaultServicesManagerImpl(dao);
        this.defaultServicesManagerImpl.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));

        final AcceptUsersAuthenticationHandler handler1 = new AcceptUsersAuthenticationHandler();
        handler1.setName("handler1");

        final AcceptUsersAuthenticationHandler handler2 = new AcceptUsersAuthenticationHandler();
        handler2.setName("handler2");

        final AcceptUsersAuthenticationHandler handler3 = new AcceptUsersAuthenticationHandler();
        handler3.setName("handler3");

        this.handlers = ImmutableSet.<AuthenticationHandler>of(handler1, handler2, handler3);
    }

    @Test
    public void checkAuthenticationHandlerResolutionDefault() {
        final RegisteredServiceAuthenticationHandlerResolver resolver =
                new RegisteredServiceAuthenticationHandlerResolver();
        resolver.setServicesManager(this.defaultServicesManagerImpl);

        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(ServicesTestUtils.getService("serviceid1"),
                ServicesTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        final Set<AuthenticationHandler> handlers =  resolver.resolve(this.handlers, transaction);
        assertEquals(handlers.size(), 2);
    }

    @Test
    public void checkAuthenticationHandlerResolution() {
        final RegisteredServiceAuthenticationHandlerResolver resolver =
                new RegisteredServiceAuthenticationHandlerResolver();
        resolver.setServicesManager(this.defaultServicesManagerImpl);
        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(ServicesTestUtils.getService("serviceid2"),
                ServicesTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        final Set<AuthenticationHandler> handlers =  resolver.resolve(this.handlers, transaction);
        assertEquals(handlers.size(), this.handlers.size());
    }
}
