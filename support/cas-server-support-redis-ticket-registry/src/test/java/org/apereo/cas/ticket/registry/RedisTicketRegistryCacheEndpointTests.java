package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasRedisCoreAutoConfiguration;
import org.apereo.cas.config.CasRedisTicketRegistryAutoConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RedisTicketRegistryCacheEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
@TestPropertySource(properties = {
    "management.endpoint.redisTicketsCache.access=UNRESTRICTED",
    "cas.ticket.registry.core.enable-locking=false"
})
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration({
    CasRedisCoreAutoConfiguration.class,
    CasRedisTicketRegistryAutoConfiguration.class
})
class RedisTicketRegistryCacheEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyCachedTicket() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val generator = new TicketGrantingTicketIdGenerator(10, "redis");
        val ticket = new TicketGrantingTicketImpl(generator.getNewTicketId(TicketGrantingTicket.PREFIX),
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        mockMvc.perform(get("/actuator/redisTicketsCache/{ticketId}", ticket.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));

        mockMvc.perform(delete("/actuator/redisTicketsCache/{ticketId}", ticket.getId())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/redisTicketsCache/{ticketId}", ticket.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyUnknownTicket() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val generator = new TicketGrantingTicketIdGenerator(10, "redis");
        val ticket = new TicketGrantingTicketImpl(generator.getNewTicketId(TicketGrantingTicket.PREFIX),
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        mockMvc.perform(get("/actuator/redisTicketsCache/{ticketId}", ticket.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    
}
