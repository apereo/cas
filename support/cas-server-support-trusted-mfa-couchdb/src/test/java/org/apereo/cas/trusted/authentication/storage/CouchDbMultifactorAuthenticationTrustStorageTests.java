package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.couchdb.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;

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
 * This is {@link CouchDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Category(CouchDbCategory.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CouchDbMultifactorAuthenticationTrustConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    MultifactorAuthnTrustWebflowConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class})
public class CouchDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("mfaTrustCouchDbConnector")
    private CouchDbConnector couchDbConnector;

    @Autowired
    @Qualifier("mfaTrustCouchDbInstance")
    private CouchDbInstance couchDbInstance;

    @Autowired
    @Qualifier("couchDbTrustRecordRepository")
    private MultifactorAuthenticationTrustRecordCouchDbRepository couchDbRepository;

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
