package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DirectObjectProvider;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the {@code CachingTicketRegistry} based on test cases to test all
 * Ticket Registries.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Tickets")
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
public class CachingTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new CachingTicketRegistry(new DirectObjectProvider<>(mock(LogoutManager.class)));
    }

    @RepeatedTest(1)
    public void verifyOtherConstructor() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                new CachingTicketRegistry(CipherExecutor.noOp(),
                    new DirectObjectProvider<>(mock(LogoutManager.class)));
            }
        });
    }

    @RepeatedTest(1)
    public void verifyExpirationByTimeout() throws Exception {
        val registry = new CachingTicketRegistry(CipherExecutor.noOp(),
            new DirectObjectProvider<>(mock(LogoutManager.class)));
        val ticket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + "-12346", RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(1));
        registry.addTicket(ticket);
        Thread.sleep(3000);
        assertNull(registry.getTicket(ticket.getId()));
    }

    @RepeatedTest(1)
    public void verifyExpirationExplicit() throws Exception {
        val registry = new CachingTicketRegistry(CipherExecutor.noOp(),
            new DirectObjectProvider<>(mock(LogoutManager.class)));
        val ticket = new MockTicketGrantingTicket("casuser");
        registry.addTicket(ticket);
        Thread.sleep(1000);
        ticket.markTicketExpired();
        assertNull(registry.getTicket(ticket.getId()));
    }
}
