package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;

import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensure cache names array initialized in Spring CacheManager.
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
    "spring.mail.port=25000",
    "spring.mail.testConnection=false"
})
public class EhCacheActuatorTests {

    @Autowired
    @Qualifier("ehcacheTicketCacheManager")
    private CacheManager ehcacheTicketCacheManager;

    @Autowired
    @Qualifier("ehCacheCacheManager")
    private EhCacheCacheManager ehCacheCacheManager;

    @Test
    public void ensureSpringCacheWrapperIsInitialized() {
        assertEquals(ehCacheCacheManager.getCacheNames().size(), ehcacheTicketCacheManager.getCacheNames().length);
    }
}
