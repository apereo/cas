package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@Import({
    CouchDbMultifactorAuthenticationTrustConfiguration.class,
    CasCouchDbCoreConfiguration.class
})
@TestPropertySource(
    properties = {
        "cas.authn.mfa.trusted.cleaner.schedule.enabled=false",
        "cas.authn.mfa.trusted.couch-db.username=cas",
        "cas.authn.mfa.trusted.couch-db.password=password"
    })
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

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

    @Test
    public void verifyExpiration() {
        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
        record.setExpirationDate(DateTimeUtils.dateOf(record.getRecordDate().plusDays(2)));
        getMfaTrustEngine().save(record);

        assertFalse(getMfaTrustEngine().get(record.getPrincipal(),
            record.getRecordDate().minusDays(1)).isEmpty());

        val now = DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).truncatedTo(ChronoUnit.SECONDS).plusDays(1);
        getMfaTrustEngine().remove(now);
        assertTrue(getMfaTrustEngine().getAll().isEmpty());
    }
}
