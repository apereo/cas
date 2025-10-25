package org.apereo.cas.apm;

import org.apereo.cas.config.CasElasticApmAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.app.ApplicationEntrypointInitializer;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import java.util.ServiceLoader;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ElasticApmAgentInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    ElasticApmAgentInitializerTests.ElasticApmTestConfiguration.class,
    CasElasticApmAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 8800)
@EnableAspectJAutoProxy
@Tag("Elastic")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMetrics
@AutoConfigureTracing
class ElasticApmAgentInitializerTests {
    static {
        System.setProperty(ElasticApmAgentInitializer.SETTING_ELASTIC_APM_AGENT_ENABLED, "yes");
    }

    @Autowired
    @Qualifier("greeter")
    private Greeter greeter;

    @Test
    void verifyOperation() {
        val agent = ServiceLoader.load(ApplicationEntrypointInitializer.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList()
            .getFirst();
        assertDoesNotThrow(() -> agent.initialize(ArrayUtils.EMPTY_STRING_ARRAY));
        assertNotNull(greeter.greet(false));
        assertThrows(IllegalArgumentException.class, () -> greeter.greet(true));

    }

    @TestConfiguration(value = "ElasticApmTestConfiguration", proxyBeanMethods = false)
    static class ElasticApmTestConfiguration {
        @Bean
        public Greeter greeter() {
            return Greeter.defaultInstance();
        }
    }
}
