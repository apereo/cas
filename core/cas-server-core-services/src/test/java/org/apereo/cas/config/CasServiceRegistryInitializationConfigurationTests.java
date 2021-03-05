package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasServiceRegistryInitializationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasServiceRegistryInitializationConfiguration.class
},
    properties = "cas.service-registry.core.init-from-json=true"
)
public class CasServiceRegistryInitializationConfigurationTests {
    @Autowired
    @Qualifier("serviceRegistryInitializer")
    private ServiceRegistryInitializer serviceRegistryInitializer;

    @Autowired
    @Qualifier("embeddedJsonServiceRegistry")
    private ServiceRegistry embeddedJsonServiceRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Test
    public void verifyOperation() {
        assertNotNull(serviceRegistryInitializer);
        assertNotNull(embeddedJsonServiceRegistry);
        assertEquals(1, servicesManager.count());
        assertNotNull(servicesManager.findServiceBy(12345));
        assertNotNull(servicesManager.findServiceBy(webApplicationServiceFactory.createService("https://init.cas.org")));
    }
}
