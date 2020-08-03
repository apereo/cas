package org.apereo.cas.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuthenticationExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class SurrogateAuthenticationExpirationPolicyBuilderTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() {
        val builder = new SurrogateAuthenticationExpirationPolicyBuilder(
            new TicketGrantingTicketExpirationPolicyBuilder(casProperties), casProperties);
        assertEquals(TicketGrantingTicket.class, builder.getTicketType());
        assertNotNull(builder.toTicketExpirationPolicy());
    }
}
