package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasAMQPTicketRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MockClock;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

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

    "cas.ticket.registry.in-memory.properties.spring.rabbitmq.management-url=http://localhost:25672",

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

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("amqpTicketRegistryClusterTopologyManager")
    private ClusterTopologyManager amqpTicketRegistryClusterTopologyManager;

    @RepeatedTest(1)
    void verifyOperation() throws Exception {
        val results = amqpTicketRegistryClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }

    
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
