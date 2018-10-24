package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.logout.LogoutManager;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the {@code CachingTicketRegistry} based on test cases to test all
 * Ticket Registries.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
})
public class CachingTicketRegistryTests extends BaseSpringRunnableTicketRegistryTests {

    public CachingTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new CachingTicketRegistry(mock(LogoutManager.class));
    }

    @Test
    public void verifyOtherConstructor() {
        assertNotNull(new DefaultTicketRegistry(10, 10, 5, CipherExecutor.noOp()));
    }
}
