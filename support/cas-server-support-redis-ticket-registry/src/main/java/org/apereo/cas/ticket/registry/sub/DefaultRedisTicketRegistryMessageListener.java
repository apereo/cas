package org.apereo.cas.ticket.registry.sub;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;
import org.apereo.cas.util.PublisherIdentifier;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    /*
     * A change made on another node only drops the copy kept here; the ticket is read from Redis when next needed.
     * Nodes running an older version still send the ticket with additions and updates, and that copy is not kept.
     */
    @Override
    public void handleMessage(final RedisMessagePayload command, final String topic) {
        if (!publisherIdentifier.equals(command.getIdentifier())) {
            LOGGER.trace("Processing Redis message payload [{}] at [{}]", command, publisherIdentifier);
            switch (command.getMessageType()) {
                case ADD, UPDATE, DELETE -> ticketCache.invalidate(command.getCacheKey());
                case DELETE_ALL -> ticketCache.invalidateAll();
            }
        }
    }
}
