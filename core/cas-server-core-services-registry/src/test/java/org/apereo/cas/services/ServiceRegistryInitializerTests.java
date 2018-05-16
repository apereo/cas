package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ServiceRegistryInitializerTests {

    @Test
    public void ensureInitFromJsonDoesNotCreateDuplicates() {
        RegisteredService initialService = CoreAuthenticationTestUtils.getRegisteredService();

        final ServicesManager servicesManager = mock(ServicesManager.class);
        final ServiceRegistry jsonServiceRegistry = mock(ServiceRegistry.class);
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        final ServiceRegistry serviceRegistry = new InMemoryServiceRegistry();
        final ServiceRegistryInitializer serviceRegistryInitializer = new ServiceRegistryInitializer(jsonServiceRegistry, serviceRegistry,
            servicesManager, true);

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);

        initialService = CoreAuthenticationTestUtils.getRegisteredService();
        when(jsonServiceRegistry.load()).thenReturn(Arrays.asList(initialService));

        serviceRegistryInitializer.initServiceRegistryIfNecessary();
        assertThat(serviceRegistry.size()).isEqualTo(1);
    }
}
