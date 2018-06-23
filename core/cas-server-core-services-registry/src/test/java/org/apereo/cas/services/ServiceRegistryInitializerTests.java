package org.apereo.cas.services;

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
        RegisteredService initialService = newService();

        final ServicesManager servicesManager = mock(ServicesManager.class);
        final ServiceRegistry jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        final ServiceRegistry serviceRegistry = new InMemoryServiceRegistry();
        final ServiceRegistryInitializer serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry, serviceRegistry, servicesManager);
        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);

        initialService = newService();
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);
    }

    private RegisteredService newService() {
        final RegisteredService service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn("^https?://.*");
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }
}
