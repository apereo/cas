package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.JsonServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonServiceRegistryConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreServicesConfiguration.class,
    JsonServiceRegistryConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
},
properties = "cas.service-registry.json.location=classpath:/services")
@Tag("FileSystem")
public class JsonServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("jsonServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    public void verifyOperation() {
        assertNotNull(this.serviceRegistry);
    }
}
