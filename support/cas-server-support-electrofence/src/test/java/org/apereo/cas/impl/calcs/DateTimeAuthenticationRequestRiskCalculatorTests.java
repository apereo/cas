package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.CloseableIterator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DateTimeAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestPropertySource(properties = {"cas.authn.adaptive.risk.date-time.enabled=true", "cas.authn.adaptive.risk.date-time.window-in-hours=4"})
@Tag("Authentication")
public class DateTimeAuthenticationRequestRiskCalculatorTests extends BaseAuthenticationRequestRiskCalculatorTests {
    @Test
    public void verifyTestWhenNoAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("datetimeperson");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isHighestRisk());
    }

    @Test
    public void verifyTestWhenAuthnEventsFoundForUser() {
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser");
        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val request = new MockHttpServletRequest();
        val score = authenticationRiskEvaluator.eval(authentication, service, request);
        assertTrue(score.isLowestRisk());
    }

    @Test
    public void verifyClosableStreamGetsClosed(){
        val list = new ArrayList<CasEvent>();
        val event = new CasEvent();
        val closeCalls = new ArrayList<Boolean>();
        event.setCreationTime("now");
        event.setId(System.currentTimeMillis());
        event.setType("someType");
        list.add(event);
        list.add(event);
        list.add(event);
        val events = new Supplier<Stream<? extends CasEvent>>() {
            @Override
            public Stream<? extends CasEvent> get() {
                return getCloseableIterator(list, closeCalls).stream();
            }
        };
        val newList = events.get().collect(Collectors.toList());
        assertTrue(!newList.isEmpty());
        assertTrue(!closeCalls.isEmpty());
        closeCalls.clear();
        val calculator = new DateTimeAuthenticationRequestRiskCalculator(casEventRepository, casProperties);
        assertTrue(!calculator.doesNotHaveEvents(events));
        assertTrue(!closeCalls.isEmpty());

    }

    private CloseableIterator<CasEvent> getCloseableIterator(final List<CasEvent> list, final List<Boolean> closeCalls) {
        return new CloseableIterator<CasEvent>(){
            private Iterator<CasEvent> iterator = list.iterator();
            @Override
            public void close() {
                closeCalls.add(true);
            }

            @Override
            public boolean hasNext() {
                val hasNext = iterator.hasNext();
                if (!hasNext){
                    this.close();
                }
                return hasNext;
            }

            @Override
            public CasEvent next() {
                val next = iterator.next();
                if (next == null){
                    this.close();
                }
                return next;
            }
        };
    }

}
