package org.apereo.cas.utils;

import org.apereo.cas.config.CassandraTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public final class TicketCatalogUtils {

    private TicketCatalogUtils() {}

    /**
     * Utility method that returns a ticket catalog for tests.
     *
     * @return ticket catalog
     */
    public static TicketCatalog getTicketCatalog() {
        final DefaultTicketCatalog ticketCatalog = new DefaultTicketCatalog();
        final DefaultTicketDefinition pt = new DefaultTicketDefinition(ProxyTicketImpl.class, "PT");
        pt.getProperties().setStorageName(CassandraTicketRegistryTicketCatalogConfiguration.ST_TABLE);
        ticketCatalog.register(pt);
        final DefaultTicketDefinition pgt = new DefaultTicketDefinition(ProxyGrantingTicketImpl.class, "PGT");
        pgt.getProperties().setStorageName(CassandraTicketRegistryTicketCatalogConfiguration.TGT_TABLE);
        ticketCatalog.register(pgt);
        final DefaultTicketDefinition st = new DefaultTicketDefinition(ServiceTicketImpl.class, "ST");
        st.getProperties().setStorageName(CassandraTicketRegistryTicketCatalogConfiguration.ST_TABLE);
        ticketCatalog.register(st);
        final DefaultTicketDefinition tgt = new DefaultTicketDefinition(TicketGrantingTicketImpl.class, "TGT");
        tgt.getProperties().setStorageName(CassandraTicketRegistryTicketCatalogConfiguration.TGT_TABLE);
        ticketCatalog.register(tgt);
        return ticketCatalog;
    }
}
