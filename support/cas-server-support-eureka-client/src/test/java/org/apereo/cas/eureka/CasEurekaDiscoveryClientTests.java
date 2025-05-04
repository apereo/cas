package org.apereo.cas.eureka;

import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasEurekaDiscoveryClientAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.config.DiscoveryClientOptionalArgsConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEurekaDiscoveryClientTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasEurekaDiscoveryClientTests {
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CasCoreWebAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasEurekaDiscoveryClientAutoConfiguration.class,
        UtilAutoConfiguration.class,
        DiscoveryClientOptionalArgsConfiguration.class,
        EurekaClientAutoConfiguration.class
    }, properties = {
        "eureka.client.tls.enabled=true",
        "eureka.client.tls.keystore=classpath:truststore.jks",
        "eureka.client.tls.keystore-password=changeit"
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Nested
    class DefaultTlsTests {
        @Autowired
        @Qualifier("eurekaClientConfigBean")
        private EurekaClientConfigBean eurekaClientConfigBean;

        @Test
        void verifyOperation() {
            assertNotNull(eurekaClientConfigBean);
        }
    }

    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CasEurekaDiscoveryClientAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        UtilAutoConfiguration.class,
        DiscoveryClientOptionalArgsConfiguration.class,
        EurekaClientAutoConfiguration.class
    }, properties = "eureka.client.tls.enabled=false")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Nested
    class CasSslTests {
        @Autowired
        @Qualifier("eurekaClientConfigBean")
        private EurekaClientConfigBean eurekaClientConfigBean;

        @Test
        void verifyOperation() {
            assertNotNull(eurekaClientConfigBean);
        }
    }
}
