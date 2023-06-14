package org.apereo.cas.web;

import org.apereo.cas.config.CasWebApplicationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasWebApplicationReadyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWebApplicationConfiguration.class
},
    properties = {
        "server.port=8588",
        "server.ssl.enabled=false",
        "cas.server.unknown.property=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
class CasWebApplicationReadyTests {
    @Test
    public void verifyOperation() {
        assertEquals(Boolean.FALSE.toString(), System.getProperty(CasWebApplicationReady.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS));
    }
}

