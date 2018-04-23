package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.LocalDate;

import static org.junit.Assert.*;


/**
 * This is {@link AbstractMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(ConditionalSpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuditConfiguration.class,
    MultifactorAuthnTrustWebflowConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class
})
public abstract class AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustEngine")
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    public void verifyTrustEngine() {
        final var record = getMultifactorAuthenticationTrustRecord();
        mfaTrustEngine.set(record);
        assertFalse(mfaTrustEngine.get(record.getPrincipal()).isEmpty());
        assertFalse(mfaTrustEngine.get(LocalDate.now()).isEmpty());
        assertFalse(mfaTrustEngine.get(record.getPrincipal(), LocalDate.now()).isEmpty());
    }

    private MultifactorAuthenticationTrustRecord getMultifactorAuthenticationTrustRecord() {
        final var record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint("Fingerprint");
        record.setName("DeviceName");
        record.setPrincipal("casuser");
        record.setId(1000);
        record.setRecordDate(LocalDate.now().plusDays(1));
        record.setRecordKey("RecordKey");
        return record;
    }
}
