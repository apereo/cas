package org.apereo.cas.services;

import org.apereo.cas.config.CosmosDbServiceRegistryConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("CosmosDb")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CosmosDbServiceRegistryConfiguration.class
}, properties = {
    "cas.serviceRegistry.cosmosDb.uri=https://localhost:8081",
    "cas.serviceRegistry.cosmosDb.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==",
    "cas.serviceRegistry.cosmosDb.database=TestDB",
    "cas.serviceRegistry.cosmosDb.dropCollection=true"
})
@EnabledIfSystemProperty(named = "cosmosDbEnabled", matches = "true")
@ResourceLock("cosmosdb-service")
public class CosmosDbServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("cosmosDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @BeforeEach
    private void deleteAll() {
        serviceRegistry.load().forEach(service -> serviceRegistry.delete(service));
        assertTrue(serviceRegistry.load().isEmpty());
    }

    @Override
    protected ServiceRegistry getNewServiceRegistry() {
        return serviceRegistry;
    }
}
