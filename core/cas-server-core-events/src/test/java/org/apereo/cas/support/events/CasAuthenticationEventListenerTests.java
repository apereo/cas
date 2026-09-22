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

    private String principalId;

    @BeforeEach
    void initialize() {
        principalId = UUID.randomUUID().toString();
        request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDR_IP);
        request.setLocalAddr(LOCAL_ADDR_IP);
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyCasAuthenticationWithNoClientInfo() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(principalId)), null);
        publishEventAndWaitToProcess(event);
        assertEquals(1, getEventsForPrincipal().size());
    }

    @Test
    void verifyCasAuthenticationWithGeo() {
        request.addHeader("geolocation", "34,45,1,12345");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(principalId)), null);
        publishEventAndWaitToProcess(event);
        val savedEvent = getEventsForPrincipal().getFirst();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(principalId)), null);
        publishEventAndWaitToProcess(event);
        val savedEvent = getEventsForPrincipal().getFirst();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    void verifyTicketGrantingTicketCreated() {
        val tgt = new MockTicketGrantingTicket(principalId);
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt, ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertEquals(1, getEventsForPrincipal().size());
    }

    @Test
    void verifyCasAuthenticationPolicyFailureEvent() {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(principalId));

        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()), transaction,
            CoreAuthenticationTestUtils.getAuthentication(principalId), null);
        publishEventAndWaitToProcess(event);
        assertEquals(1, getEventsForPrincipal().size());
    }

    @Test
    void verifyCasRiskyAuthenticationDetectedEvent() {
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(principalId),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object(), null);
        publishEventAndWaitToProcess(event);
        assertEquals(1, getEventsForPrincipal().size());
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyed() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket(principalId), ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        assertEquals(1, getEventsForPrincipal().size());
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyedHasClientInfo() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket(principalId), ClientInfoHolder.getClientInfo());
        publishEventAndWaitToProcess(event);
        val result = getEventsForPrincipal().getFirst().getClientIpAddress();
        assertEquals(REMOTE_ADDR_IP, result);
    }

    @Test
    void verifyCasTicketGrantingTicketDestroyedHasClientInfoWithMultipleThreads() throws Throwable {
        try (val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            val futureList = new ArrayList<Future<Integer>>();
            var expectedNumOfIp1 = 0;
            for (var x = 0; x < NUM_OF_REQUESTS; x++) {
                if (shouldUseIp1(x)) {
                    expectedNumOfIp1++;
                }
                futureList.add(threadPool.submit(new HttpServletRequestSimulation(x, shouldUseIp1(x), principalId, applicationContext)));
            }
            for (val future : futureList) {
                future.get();
            }

            waitForSpringEventToProcess(NUM_OF_REQUESTS);
            val list = getEventsForPrincipal();
            val numOfIp1s = (int) list.stream().filter(e -> HttpServletRequestSimulation.IP1.equals(e.getClientIpAddress())).count();
            assertEquals(NUM_OF_REQUESTS, list.size());
            assertEquals(expectedNumOfIp1, numOfIp1s);
        }
    }

    private static boolean shouldUseIp1(final int x) {
        return x % NUM_TO_USE_IP1 == 0;
    }

    private List<? extends CasEvent> getEventsForPrincipal() {
        return casEventRepository.getEventsForPrincipal(principalId).toList();
    }

    private void waitForSpringEventToProcess(final long expected) {
        await().atMost(Duration.of(2, ChronoUnit.SECONDS))
            .until(() -> casEventRepository.getEventsForPrincipal(principalId).count() >= expected);
    }

    private void publishEventAndWaitToProcess(final AbstractCasEvent event) {
        applicationContext.publishEvent(event);
        waitForSpringEventToProcess(1);
    }

    @TestConfiguration(value = "EventTestConfiguration", proxyBeanMethods = false)
    @EnableAsync(proxyTargetClass = false)
    static class EventTestConfiguration implements AsyncConfigurer {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new ConcurrentLinkedQueue<>();

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
