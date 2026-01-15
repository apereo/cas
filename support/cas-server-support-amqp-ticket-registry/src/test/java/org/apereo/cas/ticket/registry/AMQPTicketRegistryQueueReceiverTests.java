package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.DefaultQueueableTicketRegistryMessageReceiver;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.queue.AbstractTicketMessageQueueCommandTests;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AMQPTicketRegistryQueueReceiverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnabledIfListeningOnPort(port = 5672)
@Tag("AMQP")
class AMQPTicketRegistryQueueReceiverTests extends AbstractTicketMessageQueueCommandTests {
    @Test
    void verifyOperation() {
        val receiver = new DefaultQueueableTicketRegistryMessageReceiver(ticketRegistry,
            new PublisherIdentifier(UUID.randomUUID().toString()), applicationContext);
        var ticket = new TicketGrantingTicketImpl("TGT-334455", CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        val cmd = new AddTicketMessageQueueCommand(new PublisherIdentifier(), ticket);
        assertDoesNotThrow(() -> receiver.receive(cmd));
        assertNotNull(ticketRegistry.getTicket("TGT-334455"));
    }
}
