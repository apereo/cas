package org.apereo.cas.apm;

import org.apereo.cas.config.CasElasticApmAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.app.ApplicationEntrypointInitializer;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasElasticApmAutoConfiguration.class,
    ElasticApmAgentInitializerTests.ElasticApmTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 8800)
@EnableAspectJAutoProxy
@Tag("Elastic")
@AutoConfigureObservability
class ElasticApmAgentInitializerTests {
    static {
        System.setProperty(ElasticApmAgentInitializer.SETTING_ELASTIC_APM_AGENT_ENABLED, "yes");
    }

    @Autowired
    @Qualifier("greeter")
    private Greeter greeter;

    @Test
    void verifyOperation() throws Throwable {
        val agent = ServiceLoader.load(ApplicationEntrypointInitializer.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList()
            .getFirst();
        assertDoesNotThrow(() -> agent.initialize(ArrayUtils.EMPTY_STRING_ARRAY));
        assertNotNull(greeter.greet(false));
        assertThrows(IllegalArgumentException.class, () -> greeter.greet(true));

    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ElasticApmTestConfiguration {
        @Bean
        public Greeter greeter() {
            return fail -> {
                if (fail) {
                    throw new IllegalArgumentException("Failed");
                }
                return "Hello, World!";
            };
        }
    }

    @FunctionalInterface
    @Monitorable(type = "Greeting")
    private interface Greeter {
        String greet(boolean fail);
    }
}
