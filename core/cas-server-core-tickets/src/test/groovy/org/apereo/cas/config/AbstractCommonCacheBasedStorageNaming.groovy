package org.apereo.cas.config

/**
 * @author Dmitriy Kopylenko
 */
abstract class AbstractCommonCacheBasedStorageNaming extends AbstractTicketRegistryTicketCatalogConfigTests {

    @Override
    def TGT_storageNameForConcreteTicketRegistry() {
        'ticketGrantingTicketsCache'
    }

    @Override
    def ST_storageNameForConcreteTicketRegistry() {
        'serviceTicketsCache'
    }

    @Override
    def PGT_storageNameForConcreteTicketRegistry() {
        'proxyGrantingTicketsCache'
    }

    @Override
    def PT_storageNameForConcreteTicketRegistry() {
        'proxyTicketsCache'
    }
}
