package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasAMQPTicketRegistryAutoConfiguration;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MockClock;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link AMQPDefaultTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Import(AMQPDefaultTicketRegistryTests.AMQPTicketRegistryTestConfiguration.class)
@ImportAutoConfiguration({
    RabbitAutoConfiguration.class,
    CasAMQPTicketRegistryAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.ticket.registry.in-memory.crypto.signing.key=HbuPoSycjr0Pyv2u8WSwKcM6Ow0lviUdT7b9VzwxkcANqbDyKOb6KHPus_fCDCXElPhzXpeP-T0bryadZNiwOQ",
    "cas.ticket.registry.in-memory.crypto.encryption.key=BXRiSBWJcRksTizjdaCoLw",

    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672",
    "spring.rabbitmq.username=rabbituser",
    "spring.rabbitmq.password=bugsbunny"
})
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
@Getter
class AMQPDefaultTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(CipherExecutor.BEAN_NAME_TICKET_REGISTRY_CIPHER_EXECUTOR)
    private CipherExecutor messageQueueCipherExecutor;

    @Override
    protected CipherExecutor setupCipherExecutor() {
        return this.messageQueueCipherExecutor;
    }

    @TestConfiguration(value = "AMQPTicketRegistryTestConfiguration", proxyBeanMethods = false)
    static class AMQPTicketRegistryTestConfiguration {
        @Bean
        public Clock testClock() {
            return new MockClock();
        }
    }
}
