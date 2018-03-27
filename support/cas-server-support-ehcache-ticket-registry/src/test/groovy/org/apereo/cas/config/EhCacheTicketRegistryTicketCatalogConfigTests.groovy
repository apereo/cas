package org.apereo.cas.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer

/**
 * @author Dmitriy Kopylenko
 */
class EhCacheTicketRegistryTicketCatalogConfigTests extends AbstractCommonCacheBasedStorageNaming {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new EhcacheTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }
}
