package org.apereo.cas.context;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "server.port=8588",
        "server.ssl.enabled=false",
        "cas.server.unknown.property=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ContextConfiguration(initializers = CasApplicationContextInitializer.class)
@Tag("WebApp")
public class CasApplicationContextInitializerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        assertNotNull(applicationContext);
        assertEquals(Boolean.FALSE.toString(), System.getProperty(CasApplicationContextInitializer.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS));
    }
}
