package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasJsonServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonServiceRegistryConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasJsonServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = "cas.service-registry.json.location=classpath:/services")
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
class JsonServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("jsonServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    void verifyOperation() {
        assertNotNull(this.serviceRegistry);
    }
}
