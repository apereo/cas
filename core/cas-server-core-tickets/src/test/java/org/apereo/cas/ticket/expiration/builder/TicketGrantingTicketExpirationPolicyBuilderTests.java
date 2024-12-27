package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceDefinition;
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
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketGrantingTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class TicketGrantingTicketExpirationPolicyBuilderTests {

    @Test
    void verifyRememberMe() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getRememberMe().setEnabled(true);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(RememberMeDelegatingExpirationPolicy.class, builder.buildTicketExpirationPolicy());
        assertNotNull(builder.toString());
        assertNotNull(builder.casProperties());
        assertInstanceOf(RememberMeDelegatingExpirationPolicy.class, builder.buildTicketExpirationPolicyFor(mock(RegisteredServiceDefinition.class)));
    }

    @Test
    void verifyNever() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("-1");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("-1");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(NeverExpiresExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }

    @Test
    void verifyDefault() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("10");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(TicketGrantingTicketExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }

    @Test
    void verifyTimeout() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getTimeout().setMaxTimeToLiveInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(TimeoutExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }

    @Test
    void verifyHard() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getHardTimeout().setTimeToKillInSeconds("PT10S");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(HardTimeoutExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }

    @Test
    void verifyThrottle() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getThrottledTimeout().setTimeInBetweenUsesInSeconds("10");
        props.getTicket().getTgt().getThrottledTimeout().setTimeToKillInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(ThrottledUseAndTimeoutExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }

    @Test
    void verifyAlways() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("0");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("NEVER");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertInstanceOf(AlwaysExpiresExpirationPolicy.class, builder.buildTicketExpirationPolicy());
    }
}
