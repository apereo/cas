package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

/**
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@RunWith(JUnit4.class)
@DirtiesContext
public class TicketGrantingTicketExpirationPolicyTests {

    private static final long HARD_TIMEOUT = 2;
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ticketGrantingTicketExpirationPolicyTests.json");

    private static final long SLIDING_TIMEOUT = 2;

    private static final long TIMEOUT_BUFFER = 2;
    private static final String TGT_ID = "test";

    private ExpirationPolicy expirationPolicy;
    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void setUp() {
        this.expirationPolicy = new MovingTimeTicketExpirationPolicy();
        this.ticketGrantingTicket = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), this.expirationPolicy);
    }

    @Test
    public void verifyTgtIsExpiredByHardTimeOut() {
        // keep tgt alive via sliding window until within SLIDING_TIME / 2 of the HARD_TIMEOUT
        final ZonedDateTime creationTime = ticketGrantingTicket.getCreationTime();
        while (creationTime.plus(HARD_TIMEOUT - SLIDING_TIMEOUT / 2, ChronoUnit.SECONDS)
                .isAfter(org.apereo.cas.util.DateTimeUtils.zonedDateTimeOf(DateTimeUtils.currentTimeMillis()))) {
            ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(), expirationPolicy, false, true);

            final long tt = DateTimeUtils.currentTimeMillis() + ((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
            DateTimeUtils.setCurrentMillisFixed(tt);

            assertFalse(this.ticketGrantingTicket.isExpired());
        }

        // final sliding window extension past the HARD_TIMEOUT
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(), expirationPolicy, false, true);

        final long tt = DateTimeUtils.currentTimeMillis() + ((SLIDING_TIMEOUT / 2 + TIMEOUT_BUFFER) * 1_000);
        DateTimeUtils.setCurrentMillisFixed(tt);

        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    public void verifyTgtIsExpiredBySlidingWindow() {
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(), expirationPolicy, false, true);

        long tt = System.currentTimeMillis() + ((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
        DateTimeUtils.setCurrentMillisFixed(tt);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(), expirationPolicy, false, true);
        tt = DateTimeUtils.currentTimeMillis() + ((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
        DateTimeUtils.setCurrentMillisFixed(tt);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(), expirationPolicy, false, true);
        tt = DateTimeUtils.currentTimeMillis() + ((SLIDING_TIMEOUT + TIMEOUT_BUFFER) * 1_000);
        DateTimeUtils.setCurrentMillisFixed(tt);
        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    public void verifySerializeAnExpirationPolicyToJson() throws IOException {
        final TicketGrantingTicketExpirationPolicy policy = new TicketGrantingTicketExpirationPolicy(100, 100);
        MAPPER.writeValue(JSON_FILE, policy);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, TicketGrantingTicketExpirationPolicy.class);
        assertEquals(policy, policyRead);
    }

    private static class MovingTimeTicketExpirationPolicy extends TicketGrantingTicketExpirationPolicy {
        private static final long serialVersionUID = -3901717185202249332L;

        MovingTimeTicketExpirationPolicy() {
            super(HARD_TIMEOUT, SLIDING_TIMEOUT);
        }

        @Override
        protected ZonedDateTime getCurrentSystemTime() {
            return org.apereo.cas.util.DateTimeUtils.zonedDateTimeOf(DateTimeUtils.currentTimeMillis());
        }
    }
}
