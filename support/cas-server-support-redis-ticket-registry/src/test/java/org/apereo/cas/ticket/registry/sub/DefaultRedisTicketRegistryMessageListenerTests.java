package org.apereo.cas.ticket.registry.sub;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;
import org.apereo.cas.util.PublisherIdentifier;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRedisTicketRegistryMessageListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Redis")
class DefaultRedisTicketRegistryMessageListenerTests {

    @ParameterizedTest
    @EnumSource(value = RedisMessagePayload.RedisMessageTypes.class, names = {"ADD", "UPDATE", "DELETE"})
    void verifyChangeFromAnotherNodeOnlyDropsCopy(final RedisMessagePayload.RedisMessageTypes type) {
        val cache = Caffeine.newBuilder().<String, Ticket>build();
        val cached = new MockTicketGrantingTicket("casuser");
        cache.put("cache-key", cached);
        val listener = new DefaultRedisTicketRegistryMessageListener(new PublisherIdentifier(), cache);
        val sent = new MockTicketGrantingTicket("casuser");
        val payload = RedisMessagePayload.builder()
            .identifier(new PublisherIdentifier())
            .messageType(type)
            .ticket(sent)
            .cacheKey("cache-key")
            .redisKey("redis-key")
            .build();
        listener.handleMessage(payload, "topic");
        assertNull(cache.getIfPresent("cache-key"));
    }

    @ParameterizedTest
    @EnumSource(value = RedisMessagePayload.RedisMessageTypes.class, names = {"ADD", "UPDATE", "DELETE"})
    void verifyOwnChangeIsIgnored(final RedisMessagePayload.RedisMessageTypes type) {
        val cache = Caffeine.newBuilder().<String, Ticket>build();
        val cached = new MockTicketGrantingTicket("casuser");
        cache.put("cache-key", cached);
        val identifier = new PublisherIdentifier();
        val listener = new DefaultRedisTicketRegistryMessageListener(identifier, cache);
        val payload = RedisMessagePayload.builder()
            .identifier(identifier)
            .messageType(type)
            .cacheKey("cache-key")
            .build();
        listener.handleMessage(payload, "topic");
        assertSame(cached, cache.getIfPresent("cache-key"));
    }
}
