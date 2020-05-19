package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;

import lombok.SneakyThrows;
import lombok.val;
import net.sf.ehcache.distribution.CacheReplicator;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

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
public class EhCacheTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }

    @TestConfiguration("EhcacheTicketRegistryTestConfiguration")
    @Lazy(false)
    public static class EhcacheTicketRegistryTestConfiguration {
        @Bean
        @SneakyThrows
        public CacheReplicator ticketRMISynchronousCacheReplicator() {
            val replicator = mock(CacheReplicator.class);
            when(replicator.isReplicateUpdatesViaCopy()).thenReturn(false);
            when(replicator.notAlive()).thenReturn(false);
            when(replicator.alive()).thenReturn(false);
            when(replicator.clone()).thenReturn(null);
            return replicator;
        }
    }
}
