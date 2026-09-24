package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.config.CasDynamoDbMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ImportAutoConfiguration(CasDynamoDbMultifactorAuthenticationTrustAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.mfa.trusted.dynamo-db.endpoint=http://localhost:8000",
    "cas.authn.mfa.trusted.dynamo-db.drop-tables-on-startup=true",
    "cas.authn.mfa.trusted.dynamo-db.local-instance=true",
    "cas.authn.mfa.trusted.dynamo-db.region=us-east-1"
})
@Tag("DynamoDb")
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Test
    void verifySetAnExpireByKey() {
        val principal = UUID.randomUUID().toString();
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(principal,
            "geography", "fingerprint"));
        val records = getMfaTrustEngine().get(principal);
        assertEquals(1, records.size());
        getMfaTrustEngine().remove(records.stream().findFirst().get().getRecordKey());
        assertTrue(getMfaTrustEngine().get(principal).isEmpty());
    }

    @Test
    void verifyRecordsDoNotReplaceEachOther() {
        val principal = UUID.randomUUID().toString();
        val first = getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(principal,
            "geography", UUID.randomUUID().toString()));
        val second = getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(principal,
            "geography", UUID.randomUUID().toString()));
        assertNotEquals(first.getId(), second.getId());
        assertEquals(2, getMfaTrustEngine().get(principal).size());
    }

    @Test
    void verifyExpireByDate() {
        val r = MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography", "fingerprint");
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        r.setRecordDate(now.minusDays(2));
        getMfaTrustEngine().save(r);
        assertFalse(getMfaTrustEngine().get(r.getPrincipal()).isEmpty());
        assertEquals(1, countRecordsFor(r.getPrincipal(), now.minusDays(30)));
        assertEquals(0, countRecordsFor(r.getPrincipal(), now.minusDays(1)));
    }

    private long countRecordsFor(final String principal, final ZonedDateTime onOrAfterDate) {
        return getMfaTrustEngine().get(onOrAfterDate)
            .stream()
            .filter(record -> principal.equals(record.getPrincipal()))
            .count();
    }
}
