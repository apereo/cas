package org.apereo.cas.config

import org.apereo.cas.ticket.TicketCatalogConfigurer

/**
 * @author Dmitriy Kopylenko
 */
class JpaTicketRegistryTicketCatalogConfigTests extends CoreTicketCatalogConfigTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new JpaTicketRegistryTicketCatalogConfiguration()
    }
}
