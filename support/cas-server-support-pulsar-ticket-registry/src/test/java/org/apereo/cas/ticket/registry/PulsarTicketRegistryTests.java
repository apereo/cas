package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasPulsarTicketRegistryAutoConfiguration;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.pulsar.autoconfigure.PulsarAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PulsarTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ImportAutoConfiguration({
    PulsarAutoConfiguration.class,
    CasPulsarTicketRegistryAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.pulsar.transaction.enabled=false",
    "spring.pulsar.admin.service-url=http://localhost:9988",
    "spring.pulsar.client.service-url=pulsar://localhost:6650"
})
@Tag("Pulsar")
@EnabledIfListeningOnPort(port = 6650)
@Getter
@ResourceLock("ticketRegistry:pulsar")
class PulsarTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(CipherExecutor.BEAN_NAME_TICKET_REGISTRY_CIPHER_EXECUTOR)
    private CipherExecutor messageQueueCipherExecutor;

    @Autowired
    @Qualifier("ticketRegistryPulsarClusterTopologyManager")
    private ClusterTopologyManager ticketRegistryPulsarClusterTopologyManager;

    @Autowired
    @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
    private PublisherIdentifier messageQueueTicketRegistryIdentifier;

    @Autowired
    @Qualifier("messageQueueTicketRegistryPublisher")
    private QueueableTicketRegistryMessagePublisher messageQueueTicketRegistryPublisher;

    @Override
    protected CipherExecutor setupCipherExecutor() {
        return this.messageQueueCipherExecutor;
    }

    @RepeatedTest(1)
    void verifyOperation() throws Exception {
        val results = ticketRegistryPulsarClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }

    /**
     * A ticket added on another node arrives as a JSON command on the Pulsar topic and lands in this node's registry.
     */
    @RepeatedTest(1)
    void verifyTicketFromAnotherNodeIsReceived() throws Throwable {
        val ticket = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        messageQueueTicketRegistryPublisher.publishMessageToQueue(
            new AddTicketMessageQueueCommand(new PublisherIdentifier("another-node"), ticket));
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertNotNull(newTicketRegistry.getTicket(ticket.getId())));
    }

    /**
     * Each node must subscribe under a name of its own. Nodes on one exclusive subscription cannot all attach,
     * and nodes on one shared subscription split the messages, so a ticket added on one node reaches at most one other.
     */
    @RepeatedTest(1)
    void verifyEachNodeSubscribesOnItsOwn() {
        val first = CasPulsarTicketRegistryAutoConfiguration.subscriptionNameFor("cas", new PublisherIdentifier("node-1"));
        val second = CasPulsarTicketRegistryAutoConfiguration.subscriptionNameFor("cas", new PublisherIdentifier("node-2"));
        assertEquals("cas-node-1", first);
        assertNotEquals(first, second);
        assertEquals("cas-%s".formatted(messageQueueTicketRegistryIdentifier.getId()),
            CasPulsarTicketRegistryAutoConfiguration.subscriptionNameFor("cas", messageQueueTicketRegistryIdentifier));
    }
}
