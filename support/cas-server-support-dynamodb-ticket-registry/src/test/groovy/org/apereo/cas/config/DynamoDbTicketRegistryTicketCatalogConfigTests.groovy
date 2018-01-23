package org.apereo.cas.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer

/**
 * @author Dmitriy Kopylenko
 */
class DynamoDbTicketRegistryTicketCatalogConfigTests extends AbstractTicketRegistryTicketCatalogConfigTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new DynamoDbTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }

    @Override
    def TGT_storageNameForConcreteTicketRegistry() {
        'ticketGrantingTicketsTable'
    }

    @Override
    def ST_storageNameForConcreteTicketRegistry() {
        'serviceTicketsTable'
    }

    @Override
    def PGT_storageNameForConcreteTicketRegistry() {
        'proxyGrantingTicketsTable'
    }

    @Override
    def PT_storageNameForConcreteTicketRegistry() {
        'proxyTicketsTable'
    }
}
