package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pub.RedisTicketRegistryMessagePublisher;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.redis.queue-identifier=cas-node-1",
    "cas.ticket.registry.redis.host=localhost",
    "cas.ticket.registry.redis.port=6379",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.pool.enabled=true",
    "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
})
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
@Slf4j
public class RedisServerTicketRegistryTests extends BaseRedisSentinelTicketRegistryTests {
    private static final int COUNT = 1000;

    @RepeatedTest(2)
    public void verifyLargeDataset() {
        LOGGER.info("Current repetition: [{}]", useEncryption ? "Encrypted" : "Plain");
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicketToAdd = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
            })
            .limit(COUNT);
        executedTimedOperation("Adding tickets in bulk",
            Unchecked.consumer(__ -> getNewTicketRegistry().addTicket(ticketGrantingTicketToAdd)));
        executedTimedOperation("Getting tickets",
            Unchecked.consumer(__ -> {
                val tickets = getNewTicketRegistry().getTickets();
                assertFalse(tickets.isEmpty());
            }));
        val ticketStream = executedTimedOperation("Getting tickets in bulk",
            Unchecked.supplier(() -> getNewTicketRegistry().stream()));
        executedTimedOperation("Getting tickets individually",
            Unchecked.consumer(__ -> ticketStream.forEach(ticket -> assertNotNull(getNewTicketRegistry().getTicket(ticket.getId())))));

        executedTimedOperation("Counting all SSO sessions",
            Unchecked.consumer(__ -> getNewTicketRegistry().sessionCount()));
        executedTimedOperation("Counting all application sessions",
            Unchecked.consumer(__ -> getNewTicketRegistry().serviceTicketCount()));
        executedTimedOperation("Counting all user sessions",
            Unchecked.consumer(__ -> getNewTicketRegistry().countSessionsFor(authentication.getPrincipal().getId())));
    }

    private static <T> T executedTimedOperation(final String name, final Supplier<T> operation) {
        val stopwatch = new StopWatch();
        stopwatch.start();
        val result = operation.get();
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
        LOGGER.info("[{}]: [{}]ms", name, time);
        assertTrue(time <= 5000);
        return result;
    }

    private static void executedTimedOperation(final String name, final Consumer operation) {
        val stopwatch = new StopWatch();
        stopwatch.start();
        operation.accept(null);
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
        LOGGER.info("[{}]: [{}]ms", name, time);
        assertTrue(time <= 2000);
    }

    @RepeatedTest(2)
    public void verifyHealthOperation() {
        val health = redisHealthIndicator.health();
        val section = (Map) health.getDetails().get("redisTicketConnectionFactory");
        assertTrue(section.containsKey("server"));
        assertTrue(section.containsKey("memory"));
        assertTrue(section.containsKey("cpu"));
        assertTrue(section.containsKey("keyspace"));
        assertTrue(section.containsKey("stats"));
    }

    @RepeatedTest(1)
    @Tag("TicketRegistryTestWithEncryption")
    public void verifyBadTicketDecoding() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = getNewTicketRegistry().getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt);

        val cache = Caffeine.newBuilder().initialCapacity(100).<String, Ticket>build();
        val secondRegistry = new RedisTicketRegistry(ticketRedisTemplate, cache, mock(RedisTicketRegistryMessagePublisher.class));
        secondRegistry.setCipherExecutor(CipherExecutor.noOp());
        val ticket = secondRegistry.getTicket(ticketGrantingTicketId);
        assertNull(ticket);
        assertTrue(secondRegistry.getTickets().isEmpty());
        assertEquals(0, getNewTicketRegistry().stream().count());
    }

    @RepeatedTest(1)
    public void verifyFailure() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        assertNull(getNewTicketRegistry().getTicket(ticketGrantingTicketId, __ -> {
            throw new IllegalArgumentException();
        }));
        assertDoesNotThrow(() -> {
            getNewTicketRegistry().addTicket((Ticket) null);
            getNewTicketRegistry().updateTicket(null);
        });
    }

}
