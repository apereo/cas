package org.apereo.cas.services;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ServiceRegistryInitializerTests {

    @Test
    public void ensureInitFromJsonDoesNotCreateDuplicates() {
        var initialService = newService();

        val servicesManager = mock(ServicesManager.class);
        val jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        final ServiceRegistry serviceRegistry = new InMemoryServiceRegistry();
        val serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry, serviceRegistry, servicesManager);
        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);

        initialService = newService();
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);
    }

    private RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn("^https?://.*");
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }
}
