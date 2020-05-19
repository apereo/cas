package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketGrantingTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class TicketGrantingTicketExpirationPolicyBuilderTests {

    @Test
    public void verifyRememberMe() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getRememberMe().setEnabled(true);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof RememberMeDelegatingExpirationPolicy);
        assertNotNull(builder.getTicketType());
        assertNotNull(builder.toString());
        assertNotNull(builder.getCasProperties());
    }

    @Test
    public void verifyNever() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().setMaxTimeToLiveInSeconds(-1);
        props.getTicket().getTgt().setTimeToKillInSeconds(-1);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof NeverExpiresExpirationPolicy);
    }

    @Test
    public void verifyDefault() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().setMaxTimeToLiveInSeconds(10);
        props.getTicket().getTgt().setTimeToKillInSeconds(10);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof TicketGrantingTicketExpirationPolicy);
    }

    @Test
    public void verifyTimeout() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getTimeout().setMaxTimeToLiveInSeconds(10);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof TimeoutExpirationPolicy);
    }

    @Test
    public void verifyHard() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getHardTimeout().setTimeToKillInSeconds(10);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof HardTimeoutExpirationPolicy);
    }

    @Test
    public void verifyThrottle() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getThrottledTimeout().setTimeInBetweenUsesInSeconds(10);
        props.getTicket().getTgt().getThrottledTimeout().setTimeToKillInSeconds(10);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof ThrottledUseAndTimeoutExpirationPolicy);
    }

    @Test
    public void verifyAlways() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().setMaxTimeToLiveInSeconds(1);
        props.getTicket().getTgt().setTimeToKillInSeconds(-1);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof AlwaysExpiresExpirationPolicy);
    }
}
