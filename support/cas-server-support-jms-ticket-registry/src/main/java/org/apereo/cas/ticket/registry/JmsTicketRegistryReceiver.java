package org.apereo.cas.ticket.registry;

import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.queue.BaseMessageQueueCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

/**
 * This is {@link JmsTicketRegistryReceiver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JmsTicketRegistryReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsTicketRegistryReceiver.class);
    
    private final TicketRegistry ticketRegistry;
    private final StringBean ticketRegistryId;

    public JmsTicketRegistryReceiver(final TicketRegistry ticketRegistry, final StringBean ticketRegistryId) {
        this.ticketRegistry = ticketRegistry;
        this.ticketRegistryId = ticketRegistryId;
    }

    /**
     * Receive.
     *
     * @param command command to execute.
     */
    @JmsListener(destination = JmsTicketRegistry.QUEUE_DESTINATION, containerFactory = "messageQueueTicketRegistryFactory")
    public void receive(final BaseMessageQueueCommand command) {
        if (!command.getId().equals(this.ticketRegistryId)) {
            LOGGER.debug("Received message from ticket registry id [{}]. Executing command [{}]", 
                    command.getId(), command.getClass().getSimpleName());
            command.execute(this.ticketRegistry);
        } else {
            LOGGER.trace("Ignoring inbound command on ticket registry with id [{}]", this.ticketRegistryId);
        }
    }
}
