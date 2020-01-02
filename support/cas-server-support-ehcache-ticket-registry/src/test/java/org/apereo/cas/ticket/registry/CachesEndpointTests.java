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
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    MailSenderAutoConfiguration.class
}, properties = {
    "cas.ticket.registry.ehcache.maxElementsOnDisk=100",
    "cas.ticket.registry.ehcache.maxElementsInMemory=100",
    "cas.ticket.registry.ehcache.shared=true",
    "spring.mail.host=localhost",
    "spring.mail.port=25000"
})
@Tag("Ehcache")
public class CachesEndpointTests {

    @Autowired
    @Qualifier("ehcacheTicketCacheManager")
    private CacheManager ehcacheTicketCacheManager;

    @Autowired
    @Qualifier("ehCacheCacheManager")
    private EhCacheCacheManager ehCacheCacheManager;

    @Test
    void ensureCachesEndpointLoaded() {
        val endpoint = new CachesEndpoint(Collections.singletonMap("test", ehCacheCacheManager));
        assertEquals(endpoint.caches().getCacheManagers().get("test").getCaches().size(), ehcacheTicketCacheManager.getCacheNames().length);
    }

}
