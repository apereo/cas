package org.apereo.cas.support.events.couchdb;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbEventsConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import lombok.Getter;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchDbCasEventRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    CouchDbEventsConfiguration.class,
    RefreshAutoConfiguration.class
    },
    properties = {
        "cas.events.couchDb.asynchronous=false",
        "cas.events.couchDb.username=cas",
        "cas.events.couchdb.password=password"
    })
@Getter
public class CouchDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier("couchDbEventRepository")
    private EventCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
    
    @Autowired
    @Qualifier("eventCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}
