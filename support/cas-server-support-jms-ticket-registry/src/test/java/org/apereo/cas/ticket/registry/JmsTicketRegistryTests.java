package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.JmsTicketRegistryConfiguration;
import org.apereo.cas.config.JmsTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MockClock;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link JmsTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Import({
    JmsTicketRegistryTests.JmsTicketRegistryTestConfiguration.class,
    CompositeMeterRegistryAutoConfiguration.class,
    RabbitAutoConfiguration.class,
    JmsTicketRegistryTicketCatalogConfiguration.class,
    JmsTicketRegistryConfiguration.class
})
@TestPropertySource(properties = {
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672",
    "spring.rabbitmq.username=user",
    "spring.rabbitmq.password=password"
})
@EnabledIfListeningOnPort(port = 5672)
@Tag("JMS")
@Getter
public class JmsTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @TestConfiguration(value = "JmsTicketRegistryTestConfiguration", proxyBeanMethods = false)
    public static class JmsTicketRegistryTestConfiguration {
        @Bean
        public Clock testClock() {
            return new MockClock();
        }
    }
}
