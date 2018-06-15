package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.ConsentDecisionCouchDbRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchDbConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasConsentCouchDbConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasConsentCoreConfiguration.class,
    RefreshAutoConfiguration.class})
@Category(CouchDbCategory.class)
@Getter
@Slf4j
public class CouchDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentCouchDbConnector")
    private CouchDbConnector couchDbConnector;

    @Autowired
    @Qualifier("consentCouchDbInstance")
    private CouchDbInstance couchDbInstance;

    @Autowired
    @Qualifier("consentCouchDbRepository")
    private ConsentDecisionCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;

    @Before
    public void setUp() {
        couchDbInstance.createDatabaseIfNotExists(couchDbConnector.getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        couchDbInstance.deleteDatabase(couchDbConnector.getDatabaseName());
    }
}
