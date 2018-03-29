package org.apereo.cas.services;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @since 5.2.0
 */
@Slf4j
public class ServiceRegistryInitializerTests {

    @Test
    public void ensureInitFromJsonDoesNotCreateDuplicates() {
        var initialService = newService();

        final var servicesManager = mock(ServicesManager.class);
        final var jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        final ServiceRegistry serviceRegistry = new InMemoryServiceRegistry();
        final var serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry, serviceRegistry,
                                                                                                     servicesManager, true);

        // when initServiceRegistryIfNecessary is called
        serviceRegistryInitializer.initServiceRegistryIfNecessary();

        // then the initial service is added to the serviceRegistry
        assertThat(serviceRegistry.size()).isEqualTo(1);

        // given a new list of services with the same serviceId
        initialService = newService(); // create a new service object to simulate running the app multiple times
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        // when initServiceRegistryIfNecessary is called a second time
        serviceRegistryInitializer.initServiceRegistryIfNecessary();

        // then the initial service is skipped as it already exists
        assertThat(serviceRegistry.size()).isEqualTo(1);
    }


    private RegexRegisteredService newService() {
        final var service = new RegexRegisteredService();
        service.setServiceId("^https?://.*");
        service.setName("Test");
        service.setDescription("Test");
        return service;
    }
}
