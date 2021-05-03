package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.redis.host=localhost",
    "cas.ticket.registry.redis.port=6379",
    "cas.ticket.registry.redis.pool.max-active=20",
    "cas.ticket.registry.redis.pool.enabled=true",

    "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
})
@EnabledIfPortOpen(port = 6379)
@Tag("Redis")
public class RedisServerTicketRegistryTests extends BaseRedisSentinelTicketRegistryTests {

    @RepeatedTest(1)
    @Tag("TicketRegistryTestWithEncryption")
    public void verifyBadTicketDecoding() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = getNewTicketRegistry().getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt);

        val secondRegistry = new RedisTicketRegistry(ticketRedisTemplate);
        secondRegistry.setCipherExecutor(CipherExecutor.noOp());
        val ticket = secondRegistry.getTicket(ticketGrantingTicketId);
        assertNull(ticket);
        assertTrue(secondRegistry.getTickets().isEmpty());
        assertEquals(0, getNewTicketRegistry().stream().count());
    }

    @RepeatedTest(1)
    public void verifyFailure() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        assertNull(getNewTicketRegistry().getTicket(ticketGrantingTicketId, t -> {
            throw new IllegalArgumentException();
        }));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                getNewTicketRegistry().addTicket((Ticket) null);
                getNewTicketRegistry().updateTicket(null);
            }
        });
    }

}
