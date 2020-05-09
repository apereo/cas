package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbTicketRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link CouchDbTicketRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    CouchDbTicketRegistryConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.couch-db.username=cas",
        "cas.ticket.registry.couch-db.password=password"
    })
@Tag("CouchDb")
@EnabledIfPortOpen(port = 5984)
public class CouchDbTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ticketRegistryCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("ticketRegistryCouchDbRepository")
    private TicketRepository ticketRepository;

    @AfterEach
    public void afterEachTest() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        ticketRepository.initStandardDesignDocument();
        return ticketRegistry;
    }

    @Override
    protected boolean isIterableRegistry() {
        return false;
    }
}
