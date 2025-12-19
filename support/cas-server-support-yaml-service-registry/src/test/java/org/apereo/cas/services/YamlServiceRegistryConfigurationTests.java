package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasYamlServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
class YamlServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("yamlServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    void verifyOperation() {
        assertNotNull(this.serviceRegistry);
    }
}
