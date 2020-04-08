package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.config.DynamoDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
 * This is {@link DynamoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Import(DynamoDbMultifactorAuthenticationTrustConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.mfa.trusted.dynamoDb.endpoint=http://localhost:8000",
    "cas.authn.mfa.trusted.dynamoDb.dropTablesOnStartup=true",
    "cas.authn.mfa.trusted.dynamoDb.localInstance=true",
    "cas.authn.mfa.trusted.dynamoDb.region=us-east-1"
})
@Tag("DynamoDb")
@EnabledIfPortOpen(port = 8000)
public class DynamoDbMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @BeforeEach
    public void emptyTrustEngine() {
        getMfaTrustEngine().getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }

    @Test
    public void verifySetAnExpireByKey() {
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance("casuser",
            "geography", "fingerprint"));
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
        assertFalse(getMfaTrustEngine().get(r.getPrincipal()).isEmpty());
        assertEquals(1, getMfaTrustEngine().get(now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(now.minusDays(1)).size());
    }
}
