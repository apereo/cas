package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ServiceRegistryInitializerTests {

    @Test
    public void ensureInitFromJsonDoesNotCreateDuplicates() {
        val initialService = newService();

        val servicesManager = mock(ServicesManager.class);
        val jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn((Collection) Collections.singletonList(initialService));

        val serviceRegistry = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class));
        val serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry, serviceRegistry, servicesManager);
        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertEquals(1, serviceRegistry.size());

        val initialService2 = newService();
        when(jsonServiceRegistry.load()).thenReturn((Collection) Collections.singletonList(initialService2));

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertEquals(1, serviceRegistry.size());
    }

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn("^https?://.*");
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }
}
