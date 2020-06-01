package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.config.MongoDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDb")
@Import(MongoDbMultifactorAuthenticationTrustConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.authn.mfa.trusted.mongo.database-name=mfa-trusted",
        "cas.authn.mfa.trusted.mongo.host=localhost",
        "cas.authn.mfa.trusted.mongo.port=27017",
        "cas.authn.mfa.trusted.mongo.user-id=root",
        "cas.authn.mfa.trusted.mongo.password=secret",
        "cas.authn.mfa.trusted.mongo.authentication-database-name=admin",
        "cas.authn.mfa.trusted.mongo.drop-collection=true"
    })
@EnabledIfPortOpen(port = 27017)
@Getter
public class MongoDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Test
    public void verifySetAnExpireByKey() {
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint"));
        val records = getMfaTrustEngine().get("casuser");
        assertEquals(1, records.size());
        getMfaTrustEngine().remove(records.stream().findFirst().get().getRecordKey());
        assertTrue(getMfaTrustEngine().get("casuser").isEmpty());
    }

    @Test
    public void verifyExpireByDate() {
        val r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        r.setRecordDate(now.minusDays(2));
        getMfaTrustEngine().save(r);
        assertEquals(1, getMfaTrustEngine().get(now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(now.minusDays(1)).size());
    }

    @BeforeEach
    public void emptyTrustEngine() {
        getMfaTrustEngine().getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }
}
