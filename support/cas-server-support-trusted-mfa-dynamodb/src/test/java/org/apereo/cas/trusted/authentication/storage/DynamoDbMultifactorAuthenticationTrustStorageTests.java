package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.config.CasDynamoDbMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.core.SdkSystemSetting;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

    @BeforeEach
    void emptyTrustEngine() {
        getMfaTrustEngine().getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }

    @Test
    void verifySetAnExpireByKey() {
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance("casuser",
            "geography", "fingerprint"));
        val records = getMfaTrustEngine().get("casuser");
        assertEquals(1, records.size());
        getMfaTrustEngine().remove(records.stream().findFirst().get().getRecordKey());
        assertTrue(getMfaTrustEngine().get("casuser").isEmpty());
    }

    @Test
    void verifyExpireByDate() {
        val r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        r.setRecordDate(now.minusDays(2));
        getMfaTrustEngine().save(r);
        assertFalse(getMfaTrustEngine().get(r.getPrincipal()).isEmpty());
        assertEquals(1, getMfaTrustEngine().get(now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(now.minusDays(1)).size());
    }
}
