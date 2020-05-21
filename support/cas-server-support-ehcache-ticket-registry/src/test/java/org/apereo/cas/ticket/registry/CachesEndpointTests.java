package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;

import lombok.val;
import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensure CachesEndpoint is aware of all the CAS ehcache caches.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    EhcacheTicketRegistryConfiguration.class,
    EhcacheTicketRegistryTicketCatalogConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CachesEndpointAutoConfiguration.class
}, properties = {
    "cas.ticket.registry.ehcache.max-elements-on-disk=100",
    "cas.ticket.registry.ehcache.max-elements-in-memory=100",
    "cas.ticket.registry.ehcache.shared=true",
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.caches.enabled=true"
})
@Tag("Ehcache")
public class CachesEndpointTests {

    @Autowired
    @Qualifier("ehcacheTicketCacheManager")
    private CacheManager ehcacheTicketCacheManager;

    @Autowired
    @Qualifier("cachesEndpoint")
    private CachesEndpoint cachesEndpoint;

    @Test
    public void ensureCachesEndpointLoaded() {
        val cacheManagers = cachesEndpoint.caches().getCacheManagers();
        assertFalse(cacheManagers.isEmpty());
        assertTrue(cacheManagers.containsKey("ehCacheCacheManager"));
        assertEquals(cacheManagers.get("ehCacheCacheManager").getCaches().size(),
            ehcacheTicketCacheManager.getCacheNames().length);
    }

}
