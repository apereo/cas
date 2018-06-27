package org.apereo.cas.config

import org.apereo.cas.category.MongoDbCategory
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer
import org.junit.experimental.categories.Category

/**
 * @author Dmitriy Kopylenko
 */
@Category(MongoDbCategory.class)
class MongoDbTicketRegistryTicketCatalogConfigTests extends AbstractTicketRegistryTicketCatalogConfigTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new MongoDbTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }

    @Override
    def TGT_storageNameForConcreteTicketRegistry() {
        'ticketGrantingTicketsCollection'
    }

    @Override
    def ST_storageNameForConcreteTicketRegistry() {
        'serviceTicketsCollection'
    }

    @Override
    def PGT_storageNameForConcreteTicketRegistry() {
        'proxyGrantingTicketsCollection'
    }

    @Override
    def PT_storageNameForConcreteTicketRegistry() {
        'proxyTicketsCollection'
    }
}
