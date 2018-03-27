package org.apereo.cas.config

import org.apereo.cas.ticket.TicketCatalogConfigurer

class CoreTicketCatalogConfigTests extends AbstractTicketRegistryTicketCatalogConfigTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new CasCoreTicketCatalogConfiguration()
    }

    @Override
    def TGT_storageNameForConcreteTicketRegistry() {
        null
    }

    @Override
    def ST_storageNameForConcreteTicketRegistry() {
        null
    }

    @Override
    def PGT_storageNameForConcreteTicketRegistry() {
        null
    }

    @Override
    def PT_storageNameForConcreteTicketRegistry() {
        null
    }
}
