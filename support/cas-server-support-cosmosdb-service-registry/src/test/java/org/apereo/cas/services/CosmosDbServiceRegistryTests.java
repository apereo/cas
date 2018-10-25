package org.apereo.cas.services;

import org.apereo.cas.category.CosmosDbCategory;
import org.apereo.cas.config.CosmosDbServiceRegistryConfiguration;

import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Category(CosmosDbCategory.class)
@SpringBootTest(
    classes = {RefreshAutoConfiguration.class, CosmosDbServiceRegistryConfiguration.class})
@IfProfileValue(name = "cosmosDbEnabled", value = "true")
@TestPropertySource(properties = {
    "cas.serviceRegistry.cosmosDb.uri=https://localhost:8081",
    "cas.serviceRegistry.cosmosDb.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==",
    "cas.serviceRegistry.cosmosDb.database=TestDB",
    "cas.serviceRegistry.cosmosDb.dropCollection=true"
})
public class CosmosDbServiceRegistryTests {

    @Autowired
    @Qualifier("cosmosDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    private static RegisteredService buildService(final int i) {
        return RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
    }

    private void deleteAll() {
        val services = this.serviceRegistry.load();
        services.forEach(service -> this.serviceRegistry.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        deleteAll();
        assertTrue(this.serviceRegistry.load().isEmpty());
        assertTrue(this.serviceRegistry.size() == 0);

        val list = new ArrayList<RegisteredService>();
        IntStream.range(0, 5).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistry.save(list.get(i));
        });
        val results = this.serviceRegistry.load();
        assertEquals(results.size(), list.size());
        results.forEach(r -> {
            val s1 = this.serviceRegistry.findServiceById(r.getId());
            val s2 = this.serviceRegistry.findServiceById(r.getServiceId());
            assertEquals(s1, s2);
        });
        deleteAll();
        assertTrue(this.serviceRegistry.load().isEmpty());
    }
}
