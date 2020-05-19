package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
@Tag("Simple")
public class CachingTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new CachingTicketRegistry(mock(LogoutManager.class));
    }

    @Test
    @Tag("DisableEncryption")
    public void verifyOtherConstructor() {
        val registry = new CachingTicketRegistry(CipherExecutor.noOp(), mock(LogoutManager.class));
        assertNotNull(registry);
    }
}
