package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;

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
 * This is {@link CouchDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("couchdb")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CouchDbMultifactorAuthenticationTrustConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    MultifactorAuthnTrustWebflowConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.mfa.trusted.couchDb.username=cas",
    "cas.authn.mfa.trusted.couchdb.password=password"
})
@Getter
public class CouchDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("mfaTrustEngine")
    protected MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("mfaTrustCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("couchDbTrustRecordRepository")
    private MultifactorAuthenticationTrustRecordCouchDbRepository couchDbRepository;

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
