package org.apereo.cas.ticket.registry.pub;

import module java.base;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisTicketDocument;
import org.apereo.cas.ticket.registry.key.RedisKeyGenerator;
import org.apereo.cas.util.PublisherIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DefaultRedisTicketRegistryMessagePublisher}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultRedisTicketRegistryMessagePublisher implements RedisTicketRegistryMessagePublisher {
    private final CasRedisTemplate<String, RedisTicketDocument> redisTemplate;

    private final PublisherIdentifier publisherIdentifier;

    @Override
    public void deleteAll() {
        val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.DELETE_ALL);
        sendPayload(payload);
    }

    @Override
    public void delete(final Ticket ticket) {
        if (ticket != null) {
            val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.DELETE).withTicket(ticket);
            sendPayload(payload);
        }
    }

    @Override
    public void add(final Ticket ticket) {
        if (ticket != null) {
            val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.ADD).withTicket(ticket);
            sendPayload(payload);
        }
    }

    @Override
    public void update(final Ticket ticket) {
        if (ticket != null) {
            val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.UPDATE).withTicket(ticket);
            sendPayload(payload);
        }
    }

    protected void sendPayload(final RedisMessagePayload payload) {
        LOGGER.trace("Publishing Redis event payload [{}] from [{}]", payload, publisherIdentifier);
        redisTemplate.convertAndSend(RedisKeyGenerator.REDIS_TICKET_REGISTRY_MESSAGE_TOPIC, payload);
    }

    private RedisMessagePayload getRedisMessagePayload(final RedisMessagePayload.RedisMessageTypes type) {
        return RedisMessagePayload.builder()
            .messageType(type)
            .identifier(publisherIdentifier)
            .build();
    }
}
