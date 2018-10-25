package org.apereo.cas.trusted;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleaner;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.webflow.execution.Action;

import java.time.LocalDateTime;

import static org.apereo.cas.trusted.BeanNames.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link AbstractMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuditConfiguration.class,
    MultifactorAuthnTrustWebflowConfiguration.class,
    MultifactorAuthnTrustConfiguration.class,
    MultifactorAuthnTrustedDeviceFingerprintConfiguration.class
})
public abstract class AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("mfaVerifyTrustAction")
    protected Action mfaVerifyTrustAction;

    @Autowired
    @Qualifier(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    protected DeviceFingerprintStrategy deviceFingerprintStrategy;

    @Autowired
    @Qualifier("mfaTrustStorageCleaner")
    protected MultifactorAuthenticationTrustStorageCleaner mfaTrustStorageCleaner;

    protected static MultifactorAuthenticationTrustRecord getMultifactorAuthenticationTrustRecord() {
        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint("Fingerprint");
        record.setName("DeviceName");
        record.setPrincipal("casuser");
        record.setId(1000);
        record.setRecordDate(LocalDateTime.now().plusDays(1));
        record.setRecordKey("RecordKey");
        return record;
    }

    @Test
    public void verifyTrustEngine() {
        val record = getMultifactorAuthenticationTrustRecord();
        getMfaTrustEngine().set(record);
        assertFalse(getMfaTrustEngine().get(record.getPrincipal()).isEmpty());
        assertFalse(getMfaTrustEngine().get(LocalDateTime.now()).isEmpty());
        assertFalse(getMfaTrustEngine().get(record.getPrincipal(), LocalDateTime.now()).isEmpty());
    }

    public abstract MultifactorAuthenticationTrustStorage getMfaTrustEngine();
}
