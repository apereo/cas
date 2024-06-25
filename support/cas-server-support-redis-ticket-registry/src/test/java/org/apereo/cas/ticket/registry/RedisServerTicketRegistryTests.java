package org.apereo.cas.ticket.registry;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.RedisCoreConfiguration;
import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.ProxyGrantingTicketIdGenerator;
import org.apereo.cas.util.ProxyTicketIdGenerator;
import org.apereo.cas.util.ServiceTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.thread.Cleanable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
@Slf4j
class RedisServerTicketRegistryTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.ticket.registry.redis.queue-identifier=cas-node-100",
        "cas.ticket.registry.redis.host=localhost",
        "cas.ticket.registry.redis.port=6379",
        "cas.ticket.registry.redis.cache.cache-size=0",
        "cas.ticket.registry.redis.enable-redis-search=false",
        "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
        "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
    })
    class WithoutCachingTests extends BaseRedisSentinelTicketRegistryTests {
        @RepeatedTest(2)
        void verifyTrackingUsersAndPrefixes() throws Throwable {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
            val runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                            .getNewTicketId(TicketGrantingTicket.PREFIX);
                        val tgt = new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
                        getNewTicketRegistry().addTicket(tgt);

                        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
                        val stId = new ServiceTicketIdGenerator(10, StringUtils.EMPTY)
                            .getNewTicketId(ServiceTicket.PREFIX);

                        val st = tgt.grantServiceTicket(stId, service, NeverExpiresExpirationPolicy.INSTANCE,
                            false, serviceTicketSessionTrackingPolicy);
                        getNewTicketRegistry().addTicket(st);
                        getNewTicketRegistry().updateTicket(tgt);

                        assertNotNull(getNewTicketRegistry().getTicket(tgtId));
                        assertNotNull(getNewTicketRegistry().getTicket(stId));
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            val totalThreads = 25;
            val threads = new Thread[totalThreads];
            for (var i = 0; i < threads.length; i++) {
                threads[i] = new Thread(runnable);
                threads[i].start();
            }
            for (val thread : threads) {
                try {
                    thread.join();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

            val sessionCount = getNewTicketRegistry().sessionCount();
            assertEquals(totalThreads, sessionCount);

            val serviceTicketCount = getNewTicketRegistry().serviceTicketCount();
            assertEquals(totalThreads, serviceTicketCount);

            val sessions = getNewTicketRegistry().getSessionsFor(authentication.getPrincipal().getId()).toList();
            assertEquals(totalThreads, sessions.size());
            sessions.forEach(Unchecked.consumer(ticket -> {
                assertInstanceOf(TicketGrantingTicket.class, ticket);
                getNewTicketRegistry().deleteTicket(ticket);
            }));
            assertEquals(0, getNewTicketRegistry().getSessionsFor(authentication.getPrincipal().getId()).count());
            assertEquals(0, getNewTicketRegistry().sessionCount());
            assertEquals(0, getNewTicketRegistry().serviceTicketCount());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.ticket.registry.redis.queue-identifier=cas-node-100",
        "cas.ticket.registry.redis.host=localhost",
        "cas.ticket.registry.redis.port=6379",
        "cas.ticket.registry.redis.enable-redis-search=false",
        "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
        "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
    })
    class WithoutRedisModulesTests extends BaseRedisSentinelTicketRegistryTests {

    }

    @Nested
    @TestPropertySource(properties = {
        "cas.ticket.registry.redis.protocol-version=RESP2",
        "cas.ticket.registry.redis.queue-identifier=cas-node-1",
        "cas.ticket.registry.redis.host=localhost",
        "cas.ticket.registry.redis.port=6379",
        "cas.ticket.registry.redis.pool.max-active=20",
        "cas.ticket.registry.redis.pool.enabled=true",
        "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
        "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
    })
    class DefaultTests extends BaseRedisSentinelTicketRegistryTests {

        private static final int COUNT = 50;

        @RepeatedTest(2)
        void verifyLargeDataset() throws Throwable {
            LOGGER.info("Current repetition: [{}]", useEncryption ? "Encrypted" : "Plain");
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
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

        @RepeatedTest(2)
        void verifyRegistryQuery() throws Throwable {
            LOGGER.info("Current repetition: [{}]", useEncryption ? "Encrypted" : "Plain");
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
            val ticketGrantingTicketToAdd = Stream.generate(() -> {
                    val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                        .getNewTicketId(TicketGrantingTicket.PREFIX);
                    return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
                })
                .limit(5);
            getNewTicketRegistry().addTicket(ticketGrantingTicketToAdd);

            val criteria1 = new TicketRegistryQueryCriteria()
                .setCount(5L)
                .setDecode(Boolean.FALSE)
                .setType(TicketGrantingTicket.PREFIX);
            val queryResults1 = getNewTicketRegistry().query(criteria1);
            assertEquals(criteria1.getCount(), queryResults1.size());

            ((Cleanable) getNewTicketRegistry()).clean();
            val criteria2 = new TicketRegistryQueryCriteria()
                .setCount(5L)
                .setDecode(Boolean.TRUE)
                .setType(TicketGrantingTicket.PREFIX);
            val queryResults = getNewTicketRegistry().query(criteria2);
            assertEquals(criteria2.getCount(), queryResults.size());
        }

        private static <T> T executedTimedOperation(final String name, final Supplier<T> operation) {
            val stopwatch = new StopWatch();
            stopwatch.start();
            val result = operation.get();
            stopwatch.stop();
            val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.info("[{}]: [{}]ms", name, time);
            assertTrue(time <= 8000);
            return result;
        }

        private static void executedTimedOperation(final String name, final Consumer operation) {
            val stopwatch = new StopWatch();
            stopwatch.start();
            operation.accept(null);
            stopwatch.stop();
            val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
            LOGGER.info("[{}]: [{}]ms", name, time);
            assertTrue(time <= 6000);
        }

        @RepeatedTest(2)
        void verifyHealthOperation() throws Throwable {
            val health = redisHealthIndicator.health();
            val section = (Map) health.getDetails().get("redisTicketConnectionFactory");
            assertTrue(section.containsKey("server"));
            assertTrue(section.containsKey("memory"));
            assertTrue(section.containsKey("cpu"));
            assertTrue(section.containsKey("keyspace"));
            assertTrue(section.containsKey("stats"));
        }

        @RepeatedTest(1)
        void verifyFailure() throws Throwable {
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

    @Nested
    @SpringBootTest(
        classes = {
            RedisCoreConfiguration.class,
            RedisTicketRegistryConfiguration.class,
            BaseTicketRegistryTests.SharedTestConfiguration.class
        }, properties = {
            "cas.ticket.tgt.core.only-track-most-recent-session=true",
            "cas.ticket.registry.redis.host=localhost",
            "cas.ticket.registry.redis.port=6379",
            "cas.ticket.registry.redis.pool.max-active=20",
            "cas.ticket.registry.redis.pool.enabled=true",
            "cas.ticket.registry.redis.crypto.enabled=true"
        })
    class RecentSessionsTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @Test
        void verifyDifferentLoginSamePrincipal() throws Throwable {
            val principalId = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
            for (int i = 0; i < 20; i++) {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                val tgt1 = new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
                ticketRegistry.addTicket(tgt1);
            }
            assertEquals(1, ticketRegistry.countSessionsFor(principalId));
        }
    }

    @Nested
    @SpringBootTest(
        classes = {
            RedisCoreConfiguration.class,
            RedisTicketRegistryConfiguration.class,
            BaseTicketRegistryTests.SharedTestConfiguration.class
        }, properties = {
            "cas.ticket.tgt.core.only-track-most-recent-session=false",
            "cas.ticket.registry.redis.host=localhost",
            "cas.ticket.registry.redis.port=6379",
            "cas.ticket.registry.redis.crypto.enabled=false"
    })
    class TrackAllSessionsTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @Autowired
        @Qualifier("ticketRedisTemplate")
        private CasRedisTemplate<String, RedisTicketDocument> ticketRedisTemplate;

        @Test
        void verifyDifferentLoginSamePrincipal() throws Throwable {
            val principalId = UUID.randomUUID().toString();
            for (var i = 0; i < 3; i++) {
                addTicketAndWait(principalId);
            }
            val key = RedisCompositeKey.forPrincipal().withQuery(principalId).toKeyPattern();
            assertEquals(1, ticketRedisTemplate.boundZSetOps(key).size());
            assertEquals(1, ticketRegistry.countSessionsFor(principalId));
        }

        @SneakyThrows
        private void addTicketAndWait(final String principalId) {
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
            val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
            val tgt = new TicketGrantingTicketImpl(tgtId, authentication, new HardTimeoutExpirationPolicy(2));
            ticketRegistry.addTicket(tgt);

            Thread.sleep(1000);
        }
    }

    @Nested
    @SpringBootTest(
        classes = {
            RedisCoreConfiguration.class,
            RedisTicketRegistryConfiguration.class,
            BaseTicketRegistryTests.SharedTestConfiguration.class
        }, properties = {
            "cas.ticket.tgt.core.only-track-most-recent-session=true",
            "cas.ticket.registry.redis.host=localhost",
            "cas.ticket.registry.redis.port=6379"
    })
    class ConcurrentAddTicketGrantingTicketTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @Test
        void verifyConcurrentAddTicket() throws Throwable {
            val principalId = UUID.randomUUID().toString();
            val testHasFailed = new AtomicBoolean();
            val threads = new ArrayList<Thread>();
            for (var i = 1; i <= 3; i++) {
                val runnable = new RunnableAddTicketGrantingTicket(ticketRegistry, principalId, 100);
                val thread = new Thread(runnable);
                thread.setName("Thread-" + i);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LOGGER.error(e.getMessage(), e);
                    testHasFailed.set(true);
                });
                threads.add(thread);
                thread.start();
            }
            for (val thread : threads) {
                try {
                    thread.join();
                } catch (final Throwable e) {
                    fail(e);
                }
            }
            if (testHasFailed.get()) {
                fail("Test failed");
            }
        }

        @RequiredArgsConstructor
        private static final class RunnableAddTicketGrantingTicket implements Runnable {
            private final TicketRegistry ticketRegistry;
            private final String principalId;
            private final int max;

            @Override
            @SneakyThrows
            public void run() {
                val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
                val ticketGenerator = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY);
                for (int i = 0; i < max; i++) {
                    val tgtId = ticketGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
                    val tgt = new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
                    ticketRegistry.addTicket(tgt);
                }
            }
        }
    }

    @Nested
    @SpringBootTest(
            classes = {
                    RedisCoreConfiguration.class,
                    RedisTicketRegistryConfiguration.class,
                    BaseTicketRegistryTests.SharedTestConfiguration.class
            }, properties = {
            "cas.ticket.tgt.core.only-track-most-recent-session=false",
            "cas.ticket.registry.redis.host=localhost",
            "cas.ticket.registry.redis.port=6379"
    })
    class ConcurrentAddProxyTicketTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @Autowired
        @Qualifier(org.apereo.cas.ticket.tracking.TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
        private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

        @Test
        void verifyConcurrentAddTicket() throws Throwable {
            val principalId = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
            val tgtGenerator = new ProxyGrantingTicketIdGenerator(10, StringUtils.EMPTY);
            val pgt = new ProxyGrantingTicketImpl(tgtGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),
                    authentication, NeverExpiresExpirationPolicy.INSTANCE);
            ticketRegistry.addTicket(pgt);

            val request = new MockHttpServletRequest();
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com");
            val service = new WebApplicationServiceFactory().createService(request);

            val testHasFailed = new AtomicBoolean();
            val threads = new ArrayList<Thread>();
            for (var i = 1; i <= 3; i++) {
                val runnable = new RunnableAddProxyTicket(ticketRegistry, pgt, service, serviceTicketSessionTrackingPolicy, 100);
                val thread = new Thread(runnable);
                thread.setName("Thread-" + i);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    LOGGER.error(e.getMessage(), e);
                    testHasFailed.set(true);
                });
                threads.add(thread);
                thread.start();
            }
            for (val thread : threads) {
                try {
                    thread.join();
                } catch (final Throwable e) {
                    fail(e);
                }
            }
            if (testHasFailed.get()) {
                fail("Test failed");
            }
        }

        @RequiredArgsConstructor
        private static final class RunnableAddProxyTicket implements Runnable {
            private final TicketRegistry ticketRegistry;
            private final ProxyGrantingTicket proxyGrantingTicket;
            private final Service service;
            private final TicketTrackingPolicy serviceTicketSessionTrackingPolicy;
            private final int max;

            @Override
            @SneakyThrows
            public void run() {
                val ptGenerator = new ProxyTicketIdGenerator(10, StringUtils.EMPTY);
                for (int i = 0; i < max; i++) {
                    val proxyTicket = proxyGrantingTicket.grantProxyTicket(ptGenerator.getNewTicketId(ProxyTicket.PREFIX),
                            service, new HardTimeoutExpirationPolicy(20), serviceTicketSessionTrackingPolicy);
                    ticketRegistry.addTicket(proxyTicket);
                }
            }
        }
    }
}
