package org.apereo.cas.web;

import org.apereo.cas.config.CasWebApplicationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    CasWebApplicationAutoConfiguration.class
},
    properties = {
        "server.port=8588",
        "server.ssl.enabled=false",
        "cas.server.unknown.property=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasWebApplicationReadyTests {
    @Test
    void verifyOperation() throws Throwable {
        assertEquals(Boolean.FALSE.toString(), System.getProperty(CasWebApplicationReady.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS));
    }

    @Test
    void verifyWebApplication() throws Throwable {
        val sources = CasWebApplication.getApplicationSources(ArrayUtils.EMPTY_STRING_ARRAY);
        assertEquals(1, sources.size());
    }
}

