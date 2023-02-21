package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasAuthenticationEventListenerTests.EventTestConfiguration.class,
    CasCoreEventsConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@Tag("Events")
public class CasAuthenticationEventListenerTests {
    private static final String REMOTE_ADDR_IP = "123.456.789.010";
    private static final String LOCAL_ADDR_IP = "123.456.789.000";
    public static final int NUM_TO_USE_IP1 = 10;
    public static final int THREAD_POOL_SIZE = 10;
    public static final int NUM_OF_REQUESTS = 20;

    private final static AtomicInteger numEventsReceived = new AtomicInteger(0);

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    private MockHttpServletRequest request;

    @BeforeEach
    public void initialize() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDR_IP);
        request.setLocalAddr(LOCAL_ADDR_IP);
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyCasAuthenticationWithNoClientInfo() {
        ClientInfoHolder.setClientInfo(null);
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationWithGeo() {
        request.addHeader("geolocation", "34,45,1,12345");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current +1);
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    public void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    public void verifyTicketGrantingTicketCreated() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt);
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationPolicyFailureEvent() {
        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
                CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())),
            CoreAuthenticationTestUtils.getAuthentication());
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasRiskyAuthenticationDetectedEvent() {
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object());
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyed() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }
    @Test
    public void verifyEventRepositoryHasOneEventOnly() {
        clearEventRepository();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        assertEquals(1,casEventRepository.load().count());
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyedHasClientInfo() {
        clearEventRepository();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        var current = numEventsReceived.get();
        applicationContext.publishEvent(event);
        sleep(current + 1);
        val result = casEventRepository.load().toList().get(0).getClientIpAddress();
        assertEquals(REMOTE_ADDR_IP ,result);
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyedHasClientInfoWithMultipleThreads() throws Exception{
        clearEventRepository();
        var current = numEventsReceived.get();
        val threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        val futureList = new ArrayList<Future<Integer>>();
        var expectedNumOfIp1 = 0;
        for(var x = 0; x < NUM_OF_REQUESTS; x++){
            if(shouldUseIp1(x)){
                expectedNumOfIp1++;
            }
            futureList.add(threadPool.submit(new HttpServletRequestSimulation(x, shouldUseIp1(x),applicationContext)));
        }
        var maxThread = -1;
        for(var future: futureList){
           var currentThread= future.get();
           if(currentThread > maxThread){
               maxThread = currentThread;
           }
        }
        //wait 5 seconds for async threads to complete.
        sleep(current + maxThread + 1);
        val eventSize = (int) casEventRepository.load().count();
        val numOfIp1s = (int) casEventRepository.load().filter(e -> HttpServletRequestSimulation.IP1.equals(e.getClientIpAddress())).count();
        assertEquals(maxThread+1 ,eventSize);
        assertEquals(expectedNumOfIp1,numOfIp1s);
    }

    private  boolean shouldUseIp1(int x) {
        return x % NUM_TO_USE_IP1 == 0;
    }

    private void clearEventRepository(){
        casEventRepository.removeAll();
    }

    private void sleep(int expected) {
        try {
            //let async event process, so wait a bit
            while(numEventsReceived.get() < expected) {
                Thread.sleep(10L);
            }
        }catch (Exception e){
           //thread interupted..
            throw new RuntimeException("Thread interrupted");
        }
    }
    @TestConfiguration(value = "EventTestConfiguration", proxyBeanMethods = false)
    @EnableAsync
    public static class EventTestConfiguration implements AsyncConfigurer {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public CasEvent saveInternal(final CasEvent event) {
                    events.add(event);
                    numEventsReceived.addAndGet(1);
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
            ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
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
