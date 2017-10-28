package org.apereo.cas.ticket.registry;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.distribution.CacheReplicator;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.config.EhcacheTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for {@link EhCacheTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {EhCacheTicketRegistryTests.EhcacheTicketRegistryTestConfiguration.class,
        EhcacheTicketRegistryConfiguration.class,
        RefreshAutoConfiguration.class,
        EhcacheTicketRegistryTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class})
public class EhCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public EhCacheTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }


    @Configuration("EhcacheTicketRegistryTestConfiguration")
    public static class EhcacheTicketRegistryTestConfiguration {
        @Bean
        public CacheReplicator ticketRMISynchronousCacheReplicator() {
            return new NOPCacheReplicator();
        }

        private static class NOPCacheReplicator implements CacheReplicator {
            @Override
            public boolean isReplicateUpdatesViaCopy() {
                return false;
            }

            @Override
            public boolean notAlive() {
                return false;
            }

            @Override
            public boolean alive() {
                return false;
            }

            @Override
            public void notifyElementRemoved(final Ehcache ehcache, final Element element) throws CacheException {
            }

            @Override
            public void notifyElementPut(final Ehcache ehcache, final Element element) throws CacheException {
            }

            @Override
            public void notifyElementUpdated(final Ehcache ehcache, final Element element) throws CacheException {
            }

            @Override
            public void notifyElementExpired(final Ehcache ehcache, final Element element) {
            }

            @Override
            public void notifyElementEvicted(final Ehcache ehcache, final Element element) {
            }

            @Override
            public void notifyRemoveAll(final Ehcache ehcache) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object clone() {
                return null;
            }
        }
    }
}
