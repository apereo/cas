package org.apereo.cas.ticket.registry.pub;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.registry.RedisTicketDocument;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.key.RedisKeyGenerator;
import org.apereo.cas.ticket.registry.key.RedisKeyGeneratorFactory;
import org.apereo.cas.util.PublisherIdentifier;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRedisTicketRegistryMessagePublisherTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Redis")
class DefaultRedisTicketRegistryMessagePublisherTests {

    @Test
    void verifyChangesAreSentWithoutTicket() {
        val redisTemplate = (CasRedisTemplate<String, RedisTicketDocument>) mock(CasRedisTemplate.class);
        val keyGenerator = mock(RedisKeyGenerator.class);
        when(keyGenerator.forPrefixAndId(anyString(), anyString())).thenReturn("redis-key");
        val keyGeneratorFactory = mock(RedisKeyGeneratorFactory.class);
        when(keyGeneratorFactory.getRedisKeyGenerator(anyString())).thenReturn(Optional.of(keyGenerator));
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.digestIdentifier(anyString())).thenReturn("cache-key");
        val publisher = new DefaultRedisTicketRegistryMessagePublisher(redisTemplate, new PublisherIdentifier(),
            keyGeneratorFactory, ticketRegistry);

        val ticket = new MockTicketGrantingTicket("casuser");
        publisher.add(ticket);
        publisher.update(ticket);
        publisher.delete(ticket);

        val payloads = ArgumentCaptor.forClass(Object.class);
        verify(redisTemplate, times(3)).convertAndSend(eq(RedisKeyGenerator.REDIS_TICKET_REGISTRY_MESSAGE_TOPIC), payloads.capture());
        payloads.getAllValues().forEach(value -> {
            val payload = (RedisMessagePayload) value;
            assertNull(payload.getTicket());
            assertEquals(RedisMessagePayload.RedisMessageTypes.DELETE, payload.getMessageType());
            assertEquals("cache-key", payload.getCacheKey());
            assertEquals("redis-key", payload.getRedisKey());
        });
    }
}
