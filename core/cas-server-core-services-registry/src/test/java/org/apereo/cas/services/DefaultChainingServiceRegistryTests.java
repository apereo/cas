package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultChainingServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class DefaultChainingServiceRegistryTests {

    @Test
    public void verifySync() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val chain = new DefaultChainingServiceRegistry(appCtx);
        val registry = new InMemoryServiceRegistry(appCtx);
        chain.addServiceRegistry(registry);
        val service = newService();
        registry.save(service);
        chain.synchronize(service);
        assertEquals(1, registry.size());
    }

    private static RegisteredService newService() {
        val service = mock(RegisteredService.class);
        when(service.getId()).thenReturn(1234L);
        when(service.getName()).thenReturn("Test");
        when(service.getDescription()).thenReturn("Test");
        return service;
    }

}
