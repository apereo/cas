package org.apereo.cas.services;

import org.apereo.cas.config.CasJsonServiceRegistryAutoConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class JsonServiceRegistryConfigurationTests {
    @Autowired
    @Qualifier("jsonServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(this.serviceRegistry);
    }
}
