package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.StringBean;
import org.apereo.cas.ticket.registry.queue.BaseMessageQueueCommand;
import org.springframework.jms.annotation.JmsListener;

/**
 * This is {@link JmsTicketRegistryReceiver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class JmsTicketRegistryReceiver {
    private final TicketRegistry ticketRegistry;
    private final StringBean ticketRegistryId;

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
