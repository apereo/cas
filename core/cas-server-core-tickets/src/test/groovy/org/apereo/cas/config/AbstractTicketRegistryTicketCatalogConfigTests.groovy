package org.apereo.cas.config

import org.apereo.cas.ticket.*
import org.junit.Before
import org.junit.Test

/**
 * @author Dmitriy Kopylenko
 */
abstract class AbstractTicketRegistryTicketCatalogConfigTests {

    def ticketCatalog = new DefaultTicketCatalog()

    static TGT_TICKET = new TicketGrantingTicketImpl(id: 'TGT-1976')
    static ST_TICKET = new ServiceTicketImpl(id: 'ST-1976')
    static PGT_TICKET = new ProxyGrantingTicketImpl(id: 'PGT-1976')
    static PT_TICKET = new ProxyTicketImpl(id: 'PT-1976')

    abstract TicketCatalogConfigurer ticketCatalogConfigurerUnderTest()

    abstract TGT_storageNameForConcreteTicketRegistry()

    abstract ST_storageNameForConcreteTicketRegistry()

    abstract PGT_storageNameForConcreteTicketRegistry()

    abstract PT_storageNameForConcreteTicketRegistry()

    @Before
    void initialize() {
        ticketCatalogConfigurerUnderTest().configureTicketCatalog(ticketCatalog)
    }

    @Test
    void verifyTgtTicketDefinition() {
        def ticketDefinition = ticketCatalog.find(TGT_TICKET)
        assert ticketDefinition.implementationClass == TicketGrantingTicketImpl
        assert ticketDefinition.prefix == 'TGT'
        assert ticketDefinition.properties.storageName == TGT_storageNameForConcreteTicketRegistry()
    }

    @Test
    void verifyStTicketDefinition() {
        def ticketDefinition = ticketCatalog.find(ST_TICKET)
        assert ticketDefinition.implementationClass == ServiceTicketImpl
        assert ticketDefinition.prefix == 'ST'
        assert ticketDefinition.properties.storageName == ST_storageNameForConcreteTicketRegistry()
    }

    @Test
    void verifyPgtTicketDefinition() {
        def ticketDefinition = ticketCatalog.find(PGT_TICKET)
        assert ticketDefinition.implementationClass == ProxyGrantingTicketImpl
        assert ticketDefinition.prefix == 'PGT'
        assert ticketDefinition.properties.storageName == PGT_storageNameForConcreteTicketRegistry()
    }

    @Test
    void verifyPtTicketDefinition() {
        def ticketDefinition = ticketCatalog.find(PT_TICKET)
        assert ticketDefinition.implementationClass == ProxyTicketImpl
        assert ticketDefinition.prefix == 'PT'
        assert ticketDefinition.properties.storageName == PT_storageNameForConcreteTicketRegistry()
    }
}
