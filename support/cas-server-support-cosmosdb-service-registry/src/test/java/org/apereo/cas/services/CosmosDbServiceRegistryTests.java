package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CosmosDbServiceRegistryConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * This is {@link CosmosDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class, CosmosDbServiceRegistryConfiguration.class})
@IfProfileValue(name = "cosmosDbEnabled", value = "true")
@TestPropertySource(locations = {"classpath:/cosmosdb.properties"})
@Slf4j
public class CosmosDbServiceRegistryTests {

    @Autowired
    @Qualifier("cosmosDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    private void deleteAll() {
        final List<RegisteredService> services = this.serviceRegistry.load();
        services.forEach(service -> this.serviceRegistry.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        deleteAll();
        assertTrue(this.serviceRegistry.load().isEmpty());
        assertTrue(this.serviceRegistry.size() == 0);
        
        final List<RegisteredService> list = new ArrayList<>();
        IntStream.range(0, 5).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistry.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistry.load();
        assertEquals(results.size(), list.size());
        results.forEach(r -> {
            final RegisteredService s1 = this.serviceRegistry.findServiceById(r.getId());
            final RegisteredService s2 = this.serviceRegistry.findServiceById(r.getServiceId());
            assertEquals(s1, s2);
        });
        deleteAll();
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    private static RegisteredService buildService(final int i) {
        return RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
    }
}
