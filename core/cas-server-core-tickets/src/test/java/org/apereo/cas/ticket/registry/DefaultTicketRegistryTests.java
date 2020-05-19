package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
@Tag("Simple")
public class DefaultTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry(new ConcurrentHashMap<>(10, 10, 5), CipherExecutor.noOp());
    }

    @Test
    @Tag("DisableEncryption")
    public void verifyCountsUnknown() {
        val registry = mock(DefaultTicketRegistry.class);
        when(registry.getTicketsStream()).thenThrow(IllegalArgumentException.class);
        when(registry.sessionCount()).thenCallRealMethod();
        when(registry.serviceTicketCount()).thenCallRealMethod();
        assertEquals(Long.MIN_VALUE, registry.sessionCount());
        assertEquals(Long.MIN_VALUE, registry.serviceTicketCount());
    }
}
