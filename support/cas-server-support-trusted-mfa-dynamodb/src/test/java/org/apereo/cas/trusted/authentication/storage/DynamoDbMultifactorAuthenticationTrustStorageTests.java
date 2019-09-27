package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.DynamoDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    DynamoDbMultifactorAuthenticationTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.mfa.trusted.dynamoDb.endpoint=http://localhost:8000",
    "cas.authn.mfa.trusted.dynamoDb.dropTablesOnStartup=true",
    "cas.authn.mfa.trusted.dynamoDb.localInstance=true",
    "cas.authn.mfa.trusted.dynamoDb.region=us-east-1"
})
@Tag("DynamoDb")
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 8000)
public class DynamoDbMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Test
    public void verifySetAnExpireByKey() {
        mfaTrustEngine.set(MultifactorAuthenticationTrustRecord.newInstance("casuser",
            "geography", "fingerprint"));
        val records = mfaTrustEngine.get("casuser");
        assertEquals(1, records.size());
        mfaTrustEngine.expire(records.stream().findFirst().get().getRecordKey());
        assertTrue(mfaTrustEngine.get("casuser").isEmpty());
    }

    @Test
    public void verifyExpireByDate() {
        val r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        r.setRecordDate(LocalDateTime.now().minusDays(2));
        mfaTrustEngine.set(r);
        assertFalse(mfaTrustEngine.get(r.getPrincipal()).isEmpty());
        assertEquals(1, mfaTrustEngine.get(LocalDateTime.now().minusDays(30)).size());
        assertEquals(0, mfaTrustEngine.get(LocalDateTime.now().minusDays(2)).size());
    }
}
