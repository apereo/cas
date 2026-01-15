package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasKafkaTicketRegistryAutoConfiguration;
import org.apereo.cas.ticket.registry.events.KafkaMessagePublishedEvent;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceivedEvent;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.Assert;

/**
 * This is {@link KafkaTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ImportAutoConfiguration({
    KafkaAutoConfiguration.class,
    KafkaMetricsAutoConfiguration.class,
    CasKafkaTicketRegistryAutoConfiguration.class
})
@Import(KafkaTicketRegistryTests.KafkaTicketRegistryTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.ticket.registry.kafka.bootstrap-address=localhost:9092",
    "cas.ticket.registry.kafka.crypto.signing.key=HbuPoSycjr0Pyv2u8WSwKcM6Ow0lviUdT7b9VzwxkcANqbDyKOb6KHPus_fCDCXElPhzXpeP-T0bryadZNiwOQ",
    "cas.ticket.registry.kafka.crypto.encryption.key=BXRiSBWJcRksTizjdaCoLw"
})
@Tag("Kafka")
@EnabledIfListeningOnPort(port = 9092)
@Getter
@Execution(ExecutionMode.SAME_THREAD)
class KafkaTicketRegistryTests extends BaseTicketRegistryTests {
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

    @TestConfiguration(proxyBeanMethods = false)
    static class KafkaTicketRegistryTestConfiguration {
        private final CountDownLatch latch = new CountDownLatch(1);
        
        @EventListener
        public void handleKafkaMessagePublishedEvent(final KafkaMessagePublishedEvent event) throws Exception {
            val messageReceived = latch.await(5, TimeUnit.SECONDS);
            Assert.isTrue(messageReceived, "Kafka message could not be received");
        }

        @EventListener
        public void handleKafkaMessageReceivedEvent(final QueueableTicketRegistryMessageReceivedEvent event) {
            latch.countDown();
        }
    }

}
