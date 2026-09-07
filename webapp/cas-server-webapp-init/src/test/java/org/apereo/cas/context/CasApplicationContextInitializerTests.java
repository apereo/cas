package org.apereo.cas.context;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasApplicationContextInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "server.port=8588",
    "server.ssl.enabled=false"
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ContextConfiguration(initializers = CasApplicationContextInitializer.class)
@Tag("ApacheTomcat")
@ExtendWith(CasTestExtension.class)
class CasApplicationContextInitializerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @SetSystemProperty(key = CasApplicationContextInitializer.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS, value = "false")
    void verifyOperation() {
        assertNotNull(applicationContext);
        val initializer = new CasApplicationContextInitializer();
        assertDoesNotThrow(() -> initializer.initialize(applicationContext));
        assertNull(System.getProperty(CasApplicationContextInitializer.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS),
            "Context initialization must not publish a validation status; validation runs once when the application is ready");
    }
}
