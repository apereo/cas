package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServiceRegistryInitializationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class, properties = "cas.service-registry.core.init-from-json=true")
class CasServiceRegistryInitializationConfigurationTests {
    @Autowired
    @Qualifier("serviceRegistryInitializer")
    private ServiceRegistryInitializer serviceRegistryInitializer;

    @Autowired
    @Qualifier("embeddedJsonServiceRegistry")
    private ServiceRegistry embeddedJsonServiceRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Nested
    public class WithoutServiceRegistryLocation {
        @Test
        void verifyOperation() {
            assertNotNull(serviceRegistryInitializer);
            assertNotNull(embeddedJsonServiceRegistry);
            assertEquals(1, servicesManager.count());
            assertNotNull(servicesManager.findServiceBy(12345));
            val service = webApplicationServiceFactory.createService("https://init.cas.org");
            assertNotNull(servicesManager.findServiceBy(service));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.service-registry.json.location=unknown-bad-location")
    public class WithUnknownServiceRegistryLocation {
        @Test
        void verifyOperation() {
            assertNotNull(serviceRegistryInitializer);
            assertNotNull(embeddedJsonServiceRegistry);
            assertEquals(1, servicesManager.count());
            assertNotNull(servicesManager.findServiceBy(12345));
            val service = webApplicationServiceFactory.createService("https://init.cas.org");
            assertNotNull(servicesManager.findServiceBy(service));
        }
    }

}
