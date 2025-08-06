package org.apereo.cas.web;

import org.apereo.cas.config.CasCoreConfigurationMetadataAutoConfiguration;
import org.apereo.cas.config.CasWebApplicationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasWebApplicationReadyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreConfigurationMetadataAutoConfiguration.class,
    CasWebApplicationAutoConfiguration.class
}, properties = {
    "server.port=8588",
    "server.ssl.enabled=false",
    "cas.authn.mfa.gauth.issuer=CAS",
    "cas.server.unknown.property=false"
},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasWebApplicationReadyTests {
    @Test
    void verifyOperation() {
        assertEquals(Boolean.FALSE.toString(), System.getProperty(CasWebApplicationReady.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS));
    }

    @Test
    void verifyWebApplication() {
        val sources = CasWebApplication.getApplicationSources(ArrayUtils.EMPTY_STRING_ARRAY);
        assertEquals(1, sources.size());
    }
}

