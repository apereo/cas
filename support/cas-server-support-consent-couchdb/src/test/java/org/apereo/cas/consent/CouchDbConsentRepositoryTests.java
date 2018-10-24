package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

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
@Tag("couchdb")
@Getter
@TestPropertySource(properties = {
    "cas.consent.couchDb.username=cas",
    "cas.consent.couchdb.password=password"
})
public class CouchDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("consentCouchDbRepository")
    private ConsentDecisionCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;

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
