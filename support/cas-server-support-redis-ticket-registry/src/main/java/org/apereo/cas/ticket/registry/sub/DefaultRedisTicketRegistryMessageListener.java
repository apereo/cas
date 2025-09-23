package org.apereo.cas.ticket.registry.sub;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.key.RedisKeyGeneratorFactory;
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
    private final RedisKeyGeneratorFactory redisKeyGeneratorFactory;
    private final Cache<String, Ticket> ticketCache;

    @Override
    public void handleMessage(final RedisMessagePayload command, final String topic) {
        if (!publisherIdentifier.equals(command.getIdentifier())) {
            LOGGER.trace("Processing Redis message payload [{}] at [{}]", command, publisherIdentifier);
            switch (command.getMessageType()) {
                case ADD, UPDATE -> {
                    val result = getMessageResult(command);
                    ticketCache.put(result.ticket().getId(), result.ticket());
                }
                case DELETE -> {
                    val result = getMessageResult(command);
                    ticketCache.invalidate(result.ticket().getId());
                }
                case DELETE_ALL -> ticketCache.invalidateAll();
            }
        }
    }

    private MessageResult getMessageResult(final RedisMessagePayload command) {
        val ticket = (Ticket) command.getTicket();
        val generator = redisKeyGeneratorFactory.getRedisKeyGenerator(ticket.getPrefix()).orElseThrow();
        val redisKey = generator.forPrefixAndId(ticket.getPrefix(), ticket.getId());
        return new MessageResult(ticket, redisKey);
    }

    private record MessageResult(Ticket ticket, String redisKey) {
    }
}
