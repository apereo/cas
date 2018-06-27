package org.apereo.cas.config

import org.apereo.cas.category.IgniteCategory
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer
import org.junit.experimental.categories.Category

/**
 * @author Dmitriy Kopylenko
 */
@Category(IgniteCategory.class)
class IgniteTicketRegistryTicketCatalogConfigTests extends AbstractCommonCacheBasedStorageNamingTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new IgniteTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }
}
