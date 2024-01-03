package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.YamlServiceRegistryConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    YamlServiceRegistryConfiguration.class
},
    properties = "cas.service-registry.yaml.location=classpath:/services")
@Tag("FileSystem")
class YamlServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("yamlServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(this.serviceRegistry);
    }
}
