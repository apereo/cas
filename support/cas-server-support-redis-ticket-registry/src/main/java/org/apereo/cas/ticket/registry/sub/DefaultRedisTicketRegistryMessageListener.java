package org.apereo.cas.ticket.registry.sub;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisCompositeKey;
import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;
import org.apereo.cas.util.PublisherIdentifier;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DefaultRedisTicketRegistryMessageListener}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultRedisTicketRegistryMessageListener implements RedisTicketRegistryMessageListener {
    private final PublisherIdentifier publisherIdentifier;

    private final Cache<String, Ticket> ticketCache;

    @Override
    public void handleMessage(final RedisMessagePayload command, final String topic) {
        if (!publisherIdentifier.equals(command.getIdentifier())) {
            LOGGER.trace("Processing Redis message payload [{}] at [{}]", command, publisherIdentifier);
            switch (command.getMessageType()) {
                case ADD, UPDATE -> {
                    val ticket = (Ticket) command.getTicket();
                    val redisKey = RedisCompositeKey.forTickets().withTicketId(ticket.getPrefix(), ticket.getId());
                    ticketCache.put(redisKey.getQuery(), ticket);
                }
                case DELETE -> {
                    val ticket = (Ticket) command.getTicket();
                    val redisKey = RedisCompositeKey.forTickets().withTicketId(ticket.getPrefix(), ticket.getId());
                    ticketCache.invalidate(redisKey.getQuery());
                }
                case DELETE_ALL -> ticketCache.invalidateAll();
            }
        }
    }
}
