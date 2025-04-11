package org.apereo.cas.services;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@SpringBootTest(classes = {
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    CasJpaServiceRegistryAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class
},
    properties = "cas.jdbc.show-sql=false")
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
@Getter
class JpaServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final int COUNT = 10_000;

    @Autowired
    @Qualifier("jpaServiceRegistry")
    protected ServiceRegistry newServiceRegistry;

    @Test
    void verifyLargeDataset() {
        newServiceRegistry.save(
            () -> {
                val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true)
                    .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
                svc.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
                return svc;
            },
            result -> {
            },
            COUNT);
        var stopwatch = new StopWatch();
        stopwatch.start();
        assertEquals(newServiceRegistry.size(), newServiceRegistry.load().size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }

    @Test
    void verifySaveInStreams() {
        var servicesToImport = Stream.<RegisteredService>empty();
        for (var i = 0; i < 1000; i++) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            registeredService.setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            servicesToImport = Stream.concat(servicesToImport, Stream.of(registeredService));
        }
        var stopwatch = new StopWatch();
        newServiceRegistry.save(servicesToImport);
        stopwatch.start();
        assertEquals(newServiceRegistry.size(), newServiceRegistry.load().size());
        stopwatch.stop();
        assertTrue(stopwatch.getTime(TimeUnit.SECONDS) <= 10);
    }

    @Test
    void verifyEntityAttachment() {
        var service = buildRegisteredServiceInstance(RandomUtils.nextInt(), CasRegisteredService.class).assignIdIfNecessary();
        service = newServiceRegistry.save(service);
        assertNotNull(service);
        service = newServiceRegistry.findServiceById(service.getId(), CasRegisteredService.class);
        assertNotNull(service);
        service.setEvaluationOrder(999);
        val currentId = service.getId();
        service = newServiceRegistry.save(service);
        assertEquals(currentId, service.getId());
        service = newServiceRegistry.findServiceById(service.getId(), CasRegisteredService.class);
        assertEquals(999, service.getEvaluationOrder());
    }
}
