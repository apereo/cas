package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;

import lombok.Getter;
import lombok.val;
import net.sf.ehcache.distribution.CacheReplicator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link EhCacheTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 * @deprecated Since 6.2
 */
@SpringBootTest(classes = {
    EhCacheTicketRegistryTests.EhcacheTicketRegistryTestConfiguration.class,
    EhcacheTicketRegistryConfiguration.class,
    EhcacheTicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
}, properties = "cas.ticket.registry.ehcache.shared=true")
@Tag("Ehcache")
@Deprecated(since = "6.2.0")
@Getter
public class EhCacheTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @RepeatedTest(1)
    public void verifyDeleteNonExistingTicket() {
        assertEquals(1, newTicketRegistry.deleteTicket(new MockTicketGrantingTicket("casuser")));
    }

    @TestConfiguration("EhcacheTicketRegistryTestConfiguration")
    @Lazy(false)
    public static class EhcacheTicketRegistryTestConfiguration {
        @Bean
        public CacheReplicator ticketRMISynchronousCacheReplicator() throws Exception {
            val replicator = mock(CacheReplicator.class);
            when(replicator.isReplicateUpdatesViaCopy()).thenReturn(false);
            when(replicator.notAlive()).thenReturn(false);
            when(replicator.alive()).thenReturn(false);
            when(replicator.clone()).thenReturn(null);
            return replicator;
        }
    }
}
