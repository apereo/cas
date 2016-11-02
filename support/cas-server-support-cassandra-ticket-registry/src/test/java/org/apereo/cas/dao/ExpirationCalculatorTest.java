package org.apereo.cas.dao;

import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.apereo.cas.authentication.RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME;
import static org.junit.Assert.assertEquals;

public class ExpirationCalculatorTest {

    private static final int TTL = 20;
    private static final int TTK = 30;
    private static final int REMEMBER_ME_TTL = 40;

    private DefaultAuthentication defaultAuthentication;

    @Before
    public void setup() {
        Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("something", null);
        defaultAuthentication = new DefaultAuthentication(ZonedDateTime.now(), NullPrincipal.getInstance(), new HashMap<>(), successes);
    }

    @Test
    public void verifyThatForATGTWithSameCreatingAndLastUsedDatetimeReturnAnyOfThemPlusTTL() throws Exception {
        //given
        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("id", defaultAuthentication, new TimeoutExpirationPolicy(3000));

        //when
        ExpirationCalculator expirationCalculator = new ExpirationCalculator(TTL, TTK, 0);

        //then
        assertEquals(tgt.getLastTimeUsed().plusSeconds(TTL).toEpochSecond(), expirationCalculator.getExpiration(tgt));
    }

    @Test
    public void verifyThatForATGTWithNewestLastUsedDatetimeReturnItPlusTTL() throws Exception {
        //given
        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("id", defaultAuthentication, new TimeoutExpirationPolicy(3000));
        tgt.update();

        //when
        ExpirationCalculator expirationCalculator = new ExpirationCalculator(TTL, TTK, 0);

        //then
        assertEquals(tgt.getLastTimeUsed().plusSeconds(TTL).toEpochSecond(), expirationCalculator.getExpiration(tgt));
    }

    @Test
    public void verifyThatForARememberMeTGTExpirationIsCreationPlusRememberMe() throws Exception {
        //given
        Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("kkkk", null);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, true);
        defaultAuthentication = new DefaultAuthentication(ZonedDateTime.now(), NullPrincipal.getInstance(), attributes, successes);

        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("id", defaultAuthentication, new TimeoutExpirationPolicy(3000));

        //when
        ExpirationCalculator expirationCalculator = new ExpirationCalculator(TTL, TTK, REMEMBER_ME_TTL);

        //then
        assertEquals(tgt.getCreationTime().plusSeconds(REMEMBER_ME_TTL).toEpochSecond(), expirationCalculator.getExpiration(tgt));
    }
}