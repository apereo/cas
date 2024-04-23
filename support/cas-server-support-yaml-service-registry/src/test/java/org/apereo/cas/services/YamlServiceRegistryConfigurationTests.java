package org.apereo.cas.services;

import org.apereo.cas.config.CasYamlServiceRegistryAutoConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YamlServiceRegistryConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    CasYamlServiceRegistryAutoConfiguration.class
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
