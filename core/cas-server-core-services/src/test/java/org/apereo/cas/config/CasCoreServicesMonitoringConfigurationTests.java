package org.apereo.cas.config;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationTextPublisher;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreServicesMonitoringConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasCoreServicesMonitoringConfigurationTests.CasCoreServicesMonitoringTestConfiguration.class,
    BaseAutoConfigurationTests.SharedTestConfiguration.class
})
@Tag("RegisteredService")
@EnableAspectJAutoProxy(proxyTargetClass = false)
@AutoConfigureObservability
class CasCoreServicesMonitoringConfigurationTests {
    private static final List<String> ENTRIES = new ArrayList<>();

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.load();
        servicesManager.save(registeredService);
        assertNotNull(servicesManager.findServiceBy(registeredService.getId()));
        servicesManager.delete(registeredService.getId());
        servicesManager.deleteAll();
        assertFalse(ENTRIES.isEmpty());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CasCoreServicesMonitoringTestConfiguration {
        @Bean
        public ObservationHandler<Observation.Context> collectingObservationHandler() {
            return new ObservationTextPublisher(ENTRIES::add);
        }
    }
}
