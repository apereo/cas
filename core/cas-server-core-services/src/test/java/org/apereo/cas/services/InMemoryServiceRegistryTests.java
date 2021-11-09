package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.support.StaticApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is test cases for {@link InMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InMemoryServiceRegistryTests extends AbstractServiceRegistryTests {

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        return new InMemoryServiceRegistry(appCtx);
    }

    @Test
    public void removeNonExistingService() {
        var registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        assertTrue(getNewServiceRegistry().delete(registeredService));
    }
}
