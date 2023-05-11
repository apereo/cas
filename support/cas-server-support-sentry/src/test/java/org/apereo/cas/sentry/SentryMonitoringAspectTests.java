package org.apereo.cas.sentry;

import org.apereo.cas.config.CasSentryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.Monitorable;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import io.sentry.spring.boot.jakarta.SentryAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SentryMonitoringAspectTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    SentryAutoConfiguration.class,
    CasSentryConfiguration.class,
    SentryMonitoringAspectTests.SentryMonitoringTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy
@Tag("Simple")
public class SentryMonitoringAspectTests {

    @Autowired
    @Qualifier("greeter")
    private Greeter greeter;

    @Test
    public void verifyOperation() {
        assertNotNull(greeter.greet(false));
        assertThrows(IllegalArgumentException.class, () -> greeter.greet(true));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SentryMonitoringTestConfiguration {
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
