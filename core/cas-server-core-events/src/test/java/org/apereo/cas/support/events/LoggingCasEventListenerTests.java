package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.listener.LoggingCasEventListener;
import org.apereo.cas.support.events.ticket.CasProxyTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketGrantedEvent;
import org.apereo.cas.support.events.ticket.CasServiceTicketValidatedEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LoggingCasEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    DefaultCasEventListenerTests.EventTestConfiguration.class,
    CasCoreEventsConfiguration.class,
    RefreshAutoConfiguration.class
})
@Tag("Simple")
public class LoggingCasEventListenerTests {
    private final DefaultUniqueTicketIdGenerator idGenerator = new DefaultUniqueTicketIdGenerator();

    @Autowired
    @Qualifier("loggingCasEventListener")
    private LoggingCasEventListener loggingCasEventListener;

    @Test
    public void verifyPrincipalResolved() {
        val event = new CasAuthenticationPrincipalResolvedEvent(this, CoreAuthenticationTestUtils.getPrincipal());
        assertTrue(FunctionUtils.doWithoutThrows(o -> loggingCasEventListener.logAuthenticationPrincipalResolvedEvent(event)));
    }

    @Test
    public void verifyStGranted() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customService");
        val st = new MockServiceTicket("123456", service, tgt);

        val event = new CasServiceTicketGrantedEvent(this, tgt, st);
        assertTrue(FunctionUtils.doWithoutThrows(o -> loggingCasEventListener.logServiceTicketGrantedEvent(event)));
    }

    @Test
    public void verifyStValidated() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customService");
        val st = new MockServiceTicket("123456", service, tgt);
        val assertion = mock(Assertion.class);

        val event = new CasServiceTicketValidatedEvent(this, st, assertion);
        assertTrue(FunctionUtils.doWithoutThrows(o -> loggingCasEventListener.logServiceTicketValidatedEvent(event)));
    }

    @Test
    public void verifyPtGranted() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customService");
        val st = new MockServiceTicket("123456", service, tgt);

        val pgt = st.grantProxyGrantingTicket(idGenerator.getNewTicketId(ProxyGrantingTicket.PREFIX),
            tgt.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val pt = pgt.grantProxyTicket(idGenerator.getNewTicketId(ProxyTicket.PREFIX),
            CoreAuthenticationTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE, false);

        val event = new CasProxyTicketGrantedEvent(this, pgt, pt);
        assertTrue(FunctionUtils.doWithoutThrows(o -> loggingCasEventListener.logProxyTicketGrantedEvent(event)));
    }

    @Test
    public void verifyTgtRemoved() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketDestroyedEvent(this, tgt);
        assertTrue(FunctionUtils.doWithoutThrows(o -> loggingCasEventListener.logTicketGrantingTicketDestroyedEvent(event)));
    }
}
