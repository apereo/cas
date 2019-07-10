package org.apereo.cas.config

import org.apereo.cas.ticket.*
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket
import org.apereo.cas.ticket.proxy.ProxyTicket
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Dmitriy Kopylenko
 */
abstract class AbstractTicketRegistryTicketCatalogConfigTests extends Specification {

    @Shared TicketCatalog ticketCatalog = new DefaultTicketCatalog()

    static TGT_TICKET = new TicketGrantingTicketImpl(id: 'TGT-1976')

    static ST_TICKET = new ServiceTicketImpl(id: 'ST-1976')

    static PGT_TICKET = new ProxyGrantingTicketImpl(id: 'PGT-1976')

    static PT_TICKET = new ProxyTicketImpl(id: 'PT-1976')

    abstract TicketCatalogConfigurer ticketCatalogConfigurerUnderTest()

    abstract TGT_storageNameForConcreteTicketRegistry()

    abstract ST_storageNameForConcreteTicketRegistry()

    abstract PGT_storageNameForConcreteTicketRegistry()

    abstract PT_storageNameForConcreteTicketRegistry()

    def setupSpec() {
        ticketCatalogConfigurerUnderTest().configureTicketCatalog(ticketCatalog)
    }

    def "should configure correctly TGT ticket definition"() {
        when: 'Ticket Catalog configuration is run and definition is retrieved'
        def ticketDefinition = ticketCatalog.find(TGT_TICKET)

        then: 'Correct TGT ticket definition is registered with a global `TicketCatalog`'
        assert ticketDefinition.implementationClass == TicketGrantingTicketImpl
        assert ticketDefinition.prefix == 'TGT'
        assert ticketDefinition.properties.storageName == TGT_storageNameForConcreteTicketRegistry()
    }

    def "should configure correctly ST ticket definition"() {
        when: 'Ticket Catalog configuration is run and definition is retrieved'
        def ticketDefinition = ticketCatalog.find(ST_TICKET)

        then: 'Correct ST ticket definition is registered with a global `TicketCatalog`'
        assert ticketDefinition.implementationClass == ServiceTicketImpl
        assert ticketDefinition.prefix == 'ST'
        assert ticketDefinition.properties.storageName == ST_storageNameForConcreteTicketRegistry()
    }

    def "should configure correctly PGT ticket definition"() {
        when: 'Ticket Catalog configuration is run and definition is retrieved'
        def ticketDefinition = ticketCatalog.find(PGT_TICKET)

        then: 'Correct PGT ticket definition is registered with a global `TicketCatalog`'
        assert ticketDefinition.implementationClass == ProxyGrantingTicketImpl
        assert ticketDefinition.prefix == 'PGT'
        assert ticketDefinition.properties.storageName == PGT_storageNameForConcreteTicketRegistry()
    }

    def "should configure correctly PT ticket definition"() {
        when: 'Ticket Catalog configuration is run and definition is retrieved'
        def ticketDefinition = ticketCatalog.find(PT_TICKET)

        then: 'Correct PT ticket definition is registered with a global `TicketCatalog`'
        assert ticketDefinition.implementationClass == ProxyTicketImpl
        assert ticketDefinition.prefix == 'PT'
        assert ticketDefinition.properties.storageName == PT_storageNameForConcreteTicketRegistry()
    }

    def "should find TGT definitions based on ticket class"() {
        when: 'Ticket Class is the TGT implementation class'
        def definitions = ticketCatalog.find(TGT_TICKET.class)

        then: 'find by class returns the TGT and PGT definitions'
        assert definitions.size() == 2
        assert definitions*.implementationClass as Set == [TicketGrantingTicketImpl, ProxyGrantingTicketImpl] as Set
        assert definitions*.prefix as Set == ['TGT', 'PGT'] as Set
        assert definitions*.properties*.storageName as Set == [TGT_storageNameForConcreteTicketRegistry(), PGT_storageNameForConcreteTicketRegistry()] as Set

        when: 'Ticket Class is the TGT interface'
        definitions = ticketCatalog.find(TicketGrantingTicket.class)

        then: 'find by class returns the TGT and PGT definition'
        assert definitions.size() == 2
        assert definitions*.implementationClass as Set == [TicketGrantingTicketImpl, ProxyGrantingTicketImpl] as Set
        assert definitions*.prefix as Set == ['TGT', 'PGT'] as Set
        assert definitions*.properties*.storageName as Set == [TGT_storageNameForConcreteTicketRegistry(), PGT_storageNameForConcreteTicketRegistry()] as Set
    }

    def "should find PGT definition based on ticket class"() {
        when: 'Ticket Class is PGT implementation class'
        def definitions = ticketCatalog.find(PGT_TICKET.class)

        then: 'find by class returns the PGT definition'
        assert definitions.size() == 1
        assert definitions*.implementationClass as Set == [ProxyGrantingTicketImpl]  as Set
        assert definitions*.prefix  as Set == ['PGT'] as Set
        assert definitions*.properties*.storageName as Set == [PGT_storageNameForConcreteTicketRegistry()] as Set

        when: 'Ticket Class is the PGT interface'
        definitions = ticketCatalog.find(ProxyGrantingTicket.class)

        then: 'find by class returns the PGT definition'
        assert definitions.size() == 1
        assert definitions*.implementationClass as Set == [ProxyGrantingTicketImpl] as Set
        assert definitions*.prefix as Set == ['PGT'] as Set
        assert definitions*.properties*.storageName as Set == [PGT_storageNameForConcreteTicketRegistry()] as Set
    }

    def "should find ST definitions based on ticket class"() {
        when: 'Ticket Class is ST implementation class'
        def definitions = ticketCatalog.find(ST_TICKET.class)

        then: 'find by class returns the ST and PT definitions'
        assert definitions.size() == 2
        assert definitions*.implementationClass as Set == [ServiceTicketImpl, ProxyTicketImpl] as Set
        assert definitions*.prefix as Set == ['ST', 'PT'] as Set
        assert definitions*.properties*.storageName as Set == [ST_storageNameForConcreteTicketRegistry(), PT_storageNameForConcreteTicketRegistry()] as Set

        when: 'Ticket Class is the ST interface'
        definitions = ticketCatalog.find(ServiceTicket.class)

        then: 'find by class returns the ST and PT definitions'
        assert definitions.size() == 2
        assert definitions*.implementationClass as Set == [ServiceTicketImpl, ProxyTicketImpl] as Set
        assert definitions*.prefix as Set == ['ST', 'PT'] as Set
        assert definitions*.properties*.storageName as Set == [ST_storageNameForConcreteTicketRegistry(), PT_storageNameForConcreteTicketRegistry()] as Set
    }

    def "should find PT definition based on ticket class"() {
        when: 'Ticket Class is PT implementation class'
        def definitions = ticketCatalog.find(PT_TICKET.class)

        then: 'find by class returns the PT definition'
        assert definitions.size() == 1
        assert definitions*.implementationClass as Set == [ProxyTicketImpl]  as Set
        assert definitions*.prefix  as Set == ['PT'] as Set
        assert definitions*.properties*.storageName as Set == [PT_storageNameForConcreteTicketRegistry()] as Set

        when: 'Ticket Class is the PT interface'
        definitions = ticketCatalog.find(ProxyTicket.class)

        then: 'find by class returns the PT definition'
        assert definitions.size() == 1
        assert definitions*.implementationClass as Set == [ProxyTicketImpl]  as Set
        assert definitions*.prefix  as Set == ['PT'] as Set
        assert definitions*.properties*.storageName as Set == [PT_storageNameForConcreteTicketRegistry()] as Set
    }
}
