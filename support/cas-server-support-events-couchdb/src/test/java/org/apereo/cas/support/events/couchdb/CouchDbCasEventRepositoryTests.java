package org.apereo.cas.support.events.couchdb;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbEventsConfiguration;
import org.apereo.cas.couchdb.EventCouchDbRepository;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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
    properties = {"cas.events.couchDb.asyncronous=false"})
public class CouchDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("eventCouchDbConnector")
    private CouchDbConnector couchDbConnector;

    @Autowired
    @Qualifier("eventCouchDbInstance")
    private CouchDbInstance couchDbInstance;

    @Autowired
    @Qualifier("couchDbEventRepository")
    private EventCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Before
    public void setUp() {
        couchDbInstance.createDatabaseIfNotExists(couchDbConnector.getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        couchDbInstance.deleteDatabase(couchDbConnector.getDatabaseName());
    }

    @Override
    public CasEventRepository getRepositoryInstance() {
        return casEventRepository;
    }
}
