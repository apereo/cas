package org.apereo.cas.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer

/**
 * @author Dmitriy Kopylenko
 */
class HzTicketRegistryTicketCatalogConfigTests extends AbstractCommonCacheBasedStorageNamingTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new HazelcastTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }
}
