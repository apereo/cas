package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreServicesConfiguration.class
},
    properties = "cas.jdbc.show-sql=false")
@Tag("JDBC")
@Getter
class JpaServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final int COUNT = 10_000;

    @Autowired
    @Qualifier("jpaServiceRegistry")
    protected ServiceRegistry newServiceRegistry;

    @Test
    void verifyLargeDataset() throws Throwable {
        newServiceRegistry.save(
            () -> {
                val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
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
    void verifySaveInStreams() throws Throwable {
        var servicesToImport = Stream.<RegisteredService>empty();
        for (int i = 0; i < 1000; i++) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
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
}
