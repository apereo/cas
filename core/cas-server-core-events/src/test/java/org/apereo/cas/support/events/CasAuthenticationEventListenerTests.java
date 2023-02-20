package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;


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
    private
    static final String REMOTE_ADDR_IP = "123.456.789.010";
    private static final String LOCAL_ADDR_IP = "123.456.789.000";
    public static final int INT = 50;
    public static final int NUM_TO_USE_IP1 = INT;
    public static final int THREAD_POOL_SIZE = 50;
    public static final int NUM_OF_REQUESTS = 500;
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;



    @BeforeEach
    public void initialize() {
        val request = new MockHttpServletRequest();
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
        applicationContext.publishEvent(event);
        sleep();
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        applicationContext.publishEvent(event);
        sleep();
        val savedEventOptional = casEventRepository.load().findFirst();
        assertFalse(savedEventOptional.isEmpty());
        val savedEvent = savedEventOptional.get();
        assertEquals(CasAuthenticationTransactionFailureEvent.class.getSimpleName(), savedEvent.getEventId());
    }

    @Test
    public void verifyTicketGrantingTicketCreated() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt);
        applicationContext.publishEvent(event);
        sleep();
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationPolicyFailureEvent() {
        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
                CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())),
            CoreAuthenticationTestUtils.getAuthentication());
        applicationContext.publishEvent(event);
        sleep();
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasRiskyAuthenticationDetectedEvent() {
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object());
        applicationContext.publishEvent(event);
        sleep();
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyed() {
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
            new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);
        sleep();
        assertFalse(casEventRepository.load().findAny().isEmpty());
    }
    @Test
    public void verifyEventRepositoryHasOneEventOnly() {
        clearEventRepository();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);
        sleep();
        assertEquals(1,casEventRepository.load().count());
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyedHasClientInfo() {
        clearEventRepository();
        val event = new CasTicketGrantingTicketDestroyedEvent(this,
                new MockTicketGrantingTicket("casuser"));
        applicationContext.publishEvent(event);
        sleep();
        val result = casEventRepository.load().toList().get(0).getClientIpAddress();
        assertEquals(REMOTE_ADDR_IP ,result);
    }

    @Test
    public void verifyCasTicketGrantingTicketDestroyedHasClientInfoWithMultipleThreads() throws Exception{
        clearEventRepository();
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
        Thread.sleep(5000L);
        val eventSize = (int) casEventRepository.load().count();
        val numOfIp1s = (int) casEventRepository.load().filter(e -> HttpServletRequestSimulation.IP1.equals(e.getClientIpAddress())).count();
        assertEquals(maxThread+1 ,eventSize);
        assertEquals(expectedNumOfIp1,numOfIp1s);


    }

    private static boolean shouldUseIp1(int x) {
        return x % NUM_TO_USE_IP1 == 0;
    }

    private void clearEventRepository(){
        SimpleCasEventRepository eventRepository = (SimpleCasEventRepository)  casEventRepository;
        eventRepository.removeAll();
    }

    private static void sleep() {
        try {
            //let async event process, so wait a bit
            Thread.sleep(20L);
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
            return new SimpleCasEventRepository(CasEventRepositoryFilter.noOp());
        }

        @Override
        public Executor getAsyncExecutor() {
            ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
            threadPoolTaskExecutor.initialize();
            return threadPoolTaskExecutor;
        }
    }
}
