package org.apereo.cas.trusted.authentication.storage;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MongoDbMultifactorAuthenticationTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    MongoDbMultifactorAuthenticationTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    CasCoreAuditConfiguration.class,
    RefreshAutoConfiguration.class})
@Slf4j
@TestPropertySource(locations = "classpath:trustedmongo.properties")
public class MongoDbMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    public void verifySetAnExpireByKey() {
        mfaTrustEngine.set(MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint"));
        final Set<MultifactorAuthenticationTrustRecord> records = mfaTrustEngine.get("casuser");
        assertEquals(1, records.size());
        mfaTrustEngine.expire(records.stream().findFirst().get().getRecordKey());
        assertTrue(mfaTrustEngine.get("casuser").isEmpty());
    }

    @Test
    public void verifyExpireByDate() {
        final MultifactorAuthenticationTrustRecord r = MultifactorAuthenticationTrustRecord.newInstance("castest", "geography", "fingerprint");
        r.setRecordDate(LocalDateTime.now().minusDays(2));
        mfaTrustEngine.set(r);

        assertThat(mfaTrustEngine.get(LocalDateTime.now().minusDays(30)), hasSize(1));
        assertThat(mfaTrustEngine.get(LocalDateTime.now().minusDays(2)), hasSize(0));
    }
}
