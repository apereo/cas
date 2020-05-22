package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.config.YamlServiceRegistryConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YamlServiceRegistryConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreServicesConfiguration.class,
    YamlServiceRegistryConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
},
    properties = "cas.service-registry.yaml.location=classpath:/services")
@Tag("FileSystem")
public class YamlServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("yamlServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    public void verifyOperation() {
        assertNotNull(this.serviceRegistry);
    }
}
