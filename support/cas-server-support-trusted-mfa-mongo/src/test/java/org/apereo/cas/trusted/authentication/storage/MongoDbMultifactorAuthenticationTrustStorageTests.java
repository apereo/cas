package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.config.CasMongoDbMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MongoDbMFA")
@ImportAutoConfiguration(CasMongoDbMultifactorAuthenticationTrustAutoConfiguration.class)
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
@EnabledIfListeningOnPort(port = 27017)
@Getter
class MongoDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Test
    void verifySetAnExpireByKey() {
        val principal = UUID.randomUUID().toString();
        var record = MultifactorAuthenticationTrustRecord.newInstance(principal, "geography", "fingerprint");
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));

        val records = getMfaTrustEngine().get(principal);
        assertEquals(1, records.size());
        getMfaTrustEngine().remove(records.stream().findFirst().orElseThrow().getRecordKey());
        assertTrue(getMfaTrustEngine().get(principal).isEmpty());
    }

    @Test
    void verifyExpireByDate() {
        val principal = UUID.randomUUID().toString();
        val r = MultifactorAuthenticationTrustRecord.newInstance(principal, "geography", "fingerprint");
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        r.setRecordDate(now.minusDays(2));
        getMfaTrustEngine().save(r);
        assertEquals(1, getMfaTrustEngine().get(principal, now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(principal, now.minusDays(1)).size());
    }
}
