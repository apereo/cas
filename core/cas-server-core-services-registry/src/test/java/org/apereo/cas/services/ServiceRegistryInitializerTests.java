package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ServiceRegistryInitializerTests {

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn("^https?://.*");
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }

    @Test
    public void ensureInitFromJsonDoesNotCreateDuplicates() {
        val initialService = newService();

        val servicesManager = mock(ServicesManager.class);
        val jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn(List.of(initialService));

        val serviceRegistry = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class));
        val serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry,
            new DefaultChainingServiceRegistry(mock(ApplicationEventPublisher.class), List.of(serviceRegistry)),
            servicesManager);
        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertEquals(1, serviceRegistry.size());

        val initialService2 = newService();
        when(jsonServiceRegistry.load()).thenReturn(List.of(initialService2));

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertEquals(1, serviceRegistry.size());
    }
}
