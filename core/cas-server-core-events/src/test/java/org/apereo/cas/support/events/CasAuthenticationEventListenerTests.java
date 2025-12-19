package org.apereo.cas.support.events;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasAuthenticationEventListenerTests.EventTestConfiguration.class,
    AbstractCasEventRepositoryTests.SharedTestConfiguration.class
})
@Tag("Events")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "casEventRepository", mode = ResourceAccessMode.READ_WRITE)
class CasAuthenticationEventListenerTests {
    private static final String REMOTE_ADDR_IP = "123.456.789.010";
    private static final String LOCAL_ADDR_IP = "123.456.789.000";
    private static final int NUM_TO_USE_IP1 = 10;
    private static final int THREAD_POOL_SIZE = 10;
    private static final int NUM_OF_REQUESTS = 20;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    private MockHttpServletRequest request;

    @BeforeEach
    void initialize() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDR_IP);
        request.setLocalAddr(LOCAL_ADDR_IP);
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyCasAuthenticationWithNoClientInfo() {
        assertRepositoryIsEmpty();
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()), null);
        publishEventAndWaitToProcess(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    void verifyCasAuthenticationWithGeo() {
        request.addHeader("geolocation", "34,45,1,12345");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()), null);
        publishEventAndWaitToProcess(event);
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()), null);
        publishEventAndWaitToProcess(event);
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    void verifyTicketGrantingTicketCreated() {
        assertRepositoryIsEmpty();
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt, ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    void verifyCasAuthenticationPolicyFailureEvent() {
        assertRepositoryIsEmpty();

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()), transaction,
            CoreAuthenticationTestUtils.getAuthentication(), null);
        publishEventAndWaitToProcess(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    void verifyCasRiskyAuthenticationDetectedEvent() {
        assertRepositoryIsEmpty();
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object(), null);
        publishEventAndWaitToProcess(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyed() {
        assertRepositoryIsEmpty();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"), ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }


    @Test
    void verifyEventRepositoryHasOneEventOnly() {
        assertRepositoryIsEmpty();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"), ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertNotNull(casEventRepository.load());
        val resultingCount = casEventRepository.load().count();
        assertEquals(1, resultingCount);
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyedHasClientInfo() {
        assertRepositoryIsEmpty();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"), ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertNotNull(casEventRepository.load());
        val list = casEventRepository.load().toList();
        assertFalse(list.isEmpty());
        val result = list.getFirst().getClientIpAddress();
        assertEquals(REMOTE_ADDR_IP, result);
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyedHasClientInfoWithMultipleThreads() throws Throwable {
        assertRepositoryIsEmpty();
        var currentCount = casEventRepository.load().count();
        try (val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {

            val futureList = new ArrayList<Future<Integer>>();
            var expectedNumOfIp1 = 0;
            for (var x = 0; x < NUM_OF_REQUESTS; x++) {
                if (shouldUseIp1(x)) {
                    expectedNumOfIp1++;
                }
                futureList.add(threadPool.submit(new HttpServletRequestSimulation(x, shouldUseIp1(x), applicationContext)));
            }
            var maxThread = -1;
            for (val future : futureList) {
                var currentThread = future.get();
                if (currentThread > maxThread) {
                    maxThread = currentThread;
                }
            }

            waitForSpringEventToProcess(currentCount + maxThread + 1);
            assertNotNull(casEventRepository.load());
            val list = casEventRepository.load().toList();
            assertFalse(list.isEmpty());
            val eventSize = list.size();
            val numOfIp1s = (int) list.stream().filter(e -> HttpServletRequestSimulation.IP1.equals(e.getClientIpAddress())).count();
            assertEquals(maxThread + 1, eventSize);
            assertEquals(expectedNumOfIp1, numOfIp1s);
        }
    }

    private static boolean shouldUseIp1(final int x) {
        return x % NUM_TO_USE_IP1 == 0;
    }

    private void clearEventRepository() {
        casEventRepository.removeAll();
    }

    /**
     * Pass in the number of expected events that should have been stored in the {@link CasEventRepository } after publishing a new event.
     * Wait for the repository to have that many events.  Waits for 2 seconds at most.
     *
     * @param expected The expected number of events to have been saved into the {@link CasEventRepository }
     */
    private void waitForSpringEventToProcess(final long expected) {
        await().atMost(Duration.of(2, ChronoUnit.SECONDS)).until(() -> casEventRepository.load().count() >= expected);
    }

    /**
     * Count the current number of events in the {@link CasEventRepository }
     * Publish the async event to the application context,
     * Wait for the async event to process.
     *
     * @param event The event to publish
     */
    private void publishEventAndWaitToProcess(final AbstractCasEvent event) {
        var currentCount = casEventRepository.load().count();
        applicationContext.publishEvent(event);
        waitForSpringEventToProcess(currentCount + 1);
    }

    /**
     * Verify that the casEventRepository doesn't have any events in it.
     */

    private void assertRepositoryIsEmpty() {
        clearEventRepository();
        assertTrue(casEventRepository.load().findAny().isEmpty());
    }

    @TestConfiguration(value = "EventTestConfiguration", proxyBeanMethods = false)
    @EnableAsync(proxyTargetClass = false)
    static class EventTestConfiguration implements AsyncConfigurer {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public CasEvent saveInternal(final CasEvent event) {
                    events.add(event);
                    return event;
                }

                @Override
                public void removeAll() {
                    events.clear();
                }

                @Override
                public Stream<CasEvent> load() {
                    return events.stream();
                }
            };
        }

        @Override
        public Executor getAsyncExecutor() {
            var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
            threadPoolTaskExecutor.initialize();
            return threadPoolTaskExecutor;
        }

        @Bean
        public GeoLocationService geoLocationService() {
            val mock = mock(GeoLocationService.class);
            when(mock.locate(anyString())).thenReturn(new GeoLocationResponse().setLatitude(156).setLongitude(34));
            return mock;
        }
    }
}
