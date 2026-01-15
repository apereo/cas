package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
@ActiveProfiles({"prod1", "qa1"})
class DefaultServicesManagerByEnvironmentTests extends AbstractServicesManagerTests {
    @Test
    void verifyServiceByEnvironment() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(getClass().getSimpleName());
        registeredService.setServiceId(getClass().getSimpleName());
        registeredService.setEnvironments(CollectionUtils.wrapHashSet("dev1"));
        servicesManager.save(registeredService);
        assertNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(getClass().getSimpleName())));
        assertNull(servicesManager.findServiceBy(registeredService.getId()));
        registeredService.setEnvironments(CollectionUtils.wrapHashSet("prod1"));
        servicesManager.save(registeredService);
        assertNotNull(servicesManager.findServiceBy(registeredService.getId()));
    }
}
