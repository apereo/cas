package org.apereo.cas.ticket.registry.pub;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.RedisCompositeKey;
import org.apereo.cas.ticket.registry.RedisTicketDocument;
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
    public void delete(final String id) {
        val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.DELETE).withTicket(id);
        sendPayload(payload);
    }

    @Override
    public void add(final Ticket id) {
        val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.ADD).withTicket(id);
        sendPayload(payload);
    }

    @Override
    public void update(final Ticket id) {
        val payload = getRedisMessagePayload(RedisMessagePayload.RedisMessageTypes.UPDATE).withTicket(id);
        sendPayload(payload);
    }

    private void sendPayload(final RedisMessagePayload payload) {
        LOGGER.trace("Publishing Redis event payload [{}] from [{}]", payload, publisherIdentifier);
        redisTemplate.convertAndSend(RedisCompositeKey.REDIS_TICKET_REGISTRY_MESSAGE_TOPIC, payload);
    }
    
    private RedisMessagePayload getRedisMessagePayload(final RedisMessagePayload.RedisMessageTypes type) {
        return RedisMessagePayload.builder()
            .messageType(type)
            .identifier(publisherIdentifier)
            .build();
    }
}
