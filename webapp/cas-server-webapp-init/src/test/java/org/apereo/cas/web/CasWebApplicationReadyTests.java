package org.apereo.cas.web;

import module java.base;
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
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
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
    CasWebApplicationAutoConfiguration.class,
    CasWebApplicationReadyTests.ReadyTestConfiguration.class
}, properties = {
    "server.port=8588",
    "server.ssl.enabled=false",
    "cas.authn.mfa.gauth.core.issuer=CAS"
},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("ApacheTomcat")
@ExtendWith(CasTestExtension.class)
@ResourceLock(Resources.SYSTEM_PROPERTIES)
class CasWebApplicationReadyTests {
    @Test
    void verifyOperation() {
        assertEquals(Boolean.TRUE.toString(), System.getProperty(CasWebApplicationReady.SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS));
    }

    @Test
    void verifyWebApplication() {
        val sources = CasWebApplication.getApplicationSources(ArrayUtils.EMPTY_STRING_ARRAY);
        assertEquals(1, sources.size());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ReadyTestConfiguration implements AsyncConfigurer {
        @Override
        public Executor getAsyncExecutor() {
            return Runnable::run;
        }
    }
}
