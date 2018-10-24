package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.security.auth.login.FailedLoginException;
import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultCasEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    DefaultCasEventListenerTests.TestEventConfiguration.class,
    CasCoreEventsConfiguration.class,
    RefreshAutoConfiguration.class
})
public class DefaultCasEventListenerTests {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @BeforeEach
    public void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyCasAuthenticationTransactionFailureEvent() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        eventPublisher.publishEvent(event);
        assertFalse(casEventRepository.load().isEmpty());
    }

    @Test
    public void verifyTicketGrantingTicketCreated() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt);
        eventPublisher.publishEvent(event);
        assertFalse(casEventRepository.load().isEmpty());
    }

    @Test
    public void verifyCasAuthenticationPolicyFailureEvent() {
        val event = new CasAuthenticationPolicyFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            new DefaultAuthenticationTransaction(CoreAuthenticationTestUtils.getService(),
                CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())),
            CoreAuthenticationTestUtils.getAuthentication());
        eventPublisher.publishEvent(event);
        assertFalse(casEventRepository.load().isEmpty());
    }

    @Test
    public void verifyCasRiskyAuthenticationDetectedEvent() {
        val event = new CasRiskyAuthenticationDetectedEvent(this,
            CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(),
            new Object());
        eventPublisher.publishEvent(event);
        assertFalse(casEventRepository.load().isEmpty());
    }

    @TestConfiguration
    public static class TestEventConfiguration {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository() {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public void save(final CasEvent event) {
                    events.add(event);
                }

                @Override
                public Collection<CasEvent> load() {
                    return events;
                }
            };
        }
    }
}
