package org.apereo.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class InMemoryServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return new InMemoryServiceRegistry(applicationContext);
    }

    @Test
    void removeNonExistingService() {
        var registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        assertTrue(getNewServiceRegistry().delete(registeredService));
    }
}
