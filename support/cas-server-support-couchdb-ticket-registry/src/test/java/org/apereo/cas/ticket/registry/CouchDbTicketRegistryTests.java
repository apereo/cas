package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbTicketRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CouchDbTicketRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 * @deprecated Since 7
 */
@Import({
    CasCouchDbCoreConfiguration.class,
    CouchDbTicketRegistryConfiguration.class
})
@TestPropertySource(
    properties = {
        "cas.ticket.registry.couch-db.username=cas",
        "cas.ticket.registry.couch-db.caching=false",
        "cas.ticket.registry.couch-db.password=password"
    })
@Tag("CouchDb")
@EnabledIfListeningOnPort(port = 5984)
@Deprecated(since = "7.0.0")
public class CouchDbTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistryCouchDbRepository")
    private TicketRepository ticketRepository;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ticketRegistryCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        ticketRepository.initStandardDesignDocument();
        return ticketRegistry;
    }

    @RepeatedTest(1)
    public void verifyFails() throws Exception {
        val couchDb = mock(TicketRepository.class);
        doThrow(new DbAccessException()).when(couchDb).update(any());
        val registry = new CouchDbTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog, couchDb, 1);
        assertNull(registry.updateTicket(new MockTicketGrantingTicket("casuser")));

        doThrow(new UpdateConflictException()).when(couchDb).remove(any());
        assertEquals(0, registry.deleteTicket(new MockTicketGrantingTicket("casuser")));

        doThrow(new DocumentNotFoundException("path")).when(couchDb).remove(any());
        assertEquals(0, registry.deleteTicket(new MockTicketGrantingTicket("casuser")));
    }

    @Override
    protected boolean isIterableRegistry() {
        return false;
    }
}
