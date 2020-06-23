package org.apereo.cas.trusted;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.apereo.cas.trusted.BeanNames.BEAN_DEVICE_FINGERPRINT_STRATEGY;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link AbstractMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("MFA")
@Getter
public abstract class AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustEngine")
    protected MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("mfaTrustRecordKeyGenerator")
    protected MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy;

    @Autowired
    @Qualifier("mfaVerifyTrustAction")
    protected Action mfaVerifyTrustAction;

    @Autowired
    @Qualifier("mfaSetTrustAction")
    protected Action mfaSetTrustAction;

    @Autowired
    @Qualifier("mfaPrepareTrustDeviceViewAction")
    protected Action mfaPrepareTrustDeviceViewAction;

    @Autowired
    @Qualifier(BEAN_DEVICE_FINGERPRINT_STRATEGY)
    protected DeviceFingerprintStrategy deviceFingerprintStrategy;
    
    protected static MultifactorAuthenticationTrustRecord getMultifactorAuthenticationTrustRecord() {
        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint(UUID.randomUUID().toString());
        record.setName("DeviceName");
        record.setPrincipal(UUID.randomUUID().toString());
        record.setId(1000);
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        record.setRecordKey(UUID.randomUUID().toString());
        record.setExpirationDate(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1)));
        return record;
    }

    @Test
    public void verifyTrustEngine() {
        val record = getMultifactorAuthenticationTrustRecord();
        getMfaTrustEngine().save(record);
        assertFalse(getMfaTrustEngine().getAll().isEmpty());
        assertFalse(getMfaTrustEngine().get(record.getPrincipal()).isEmpty());
        val now = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
        assertFalse(getMfaTrustEngine().get(now).isEmpty());
        assertFalse(getMfaTrustEngine().get(record.getPrincipal(), now).isEmpty());

        getMfaTrustEngine().remove(DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).plusDays(1));
        assertTrue(getMfaTrustEngine().getAll().isEmpty());
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreUtilConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreHttpConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
