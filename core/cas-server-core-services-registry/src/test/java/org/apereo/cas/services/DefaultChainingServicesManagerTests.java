package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.mgmt.DefaultChainingServicesManager;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("RegisteredService")
class DefaultChainingServicesManagerTests {

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(RandomUtils.nextLong());
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }

    private static ServicesManager newServicesManager() {
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.supports(any(Service.class))).thenReturn(true);
        when(servicesManager.supports(any(RegisteredService.class))).thenReturn(true);
        return servicesManager;
    }

    @Test
    void verifyChainServiceManager() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val servicesManager = new DefaultChainingServicesManager();
        servicesManager.registerServiceManager(newServicesManager());
        servicesManager.registerServiceManager(newServicesManager());
        servicesManager.save(Stream.of(newService(), newService(), newService()));
    }

}
