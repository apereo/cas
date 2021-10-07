package org.apereo.cas.trusted;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * This is {@link AbstractMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Getter
public abstract class AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustEngine")
    protected MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Autowired
    @Qualifier("mfaTrustRecordKeyGenerator")
    protected MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_VERIFY_TRUST_ACTION)
    protected Action mfaVerifyTrustAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
    protected Action mfaSetTrustAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION)
    protected Action mfaPrepareTrustDeviceViewAction;

    @Autowired
    @Qualifier(DeviceFingerprintStrategy.DEFAULT_BEAN_NAME)
    protected DeviceFingerprintStrategy deviceFingerprintStrategy;
    
    protected static MultifactorAuthenticationTrustRecord getMultifactorAuthenticationTrustRecord() {
        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint(UUID.randomUUID().toString());
        record.setName("DeviceName");
        record.setPrincipal(UUID.randomUUID().toString());
        record.setId(1000);
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        record.setExpirationDate(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1)));
        return record;
    }

    @Test
    public void verifyTrustEngine() {
        var record = getMultifactorAuthenticationTrustRecord();
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));
        assertFalse(getMfaTrustEngine().getAll().isEmpty());
        assertFalse(getMfaTrustEngine().get(record.getPrincipal()).isEmpty());
        val now = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
        assertFalse(getMfaTrustEngine().get(now).isEmpty());
        assertFalse(getMfaTrustEngine().get(record.getPrincipal(), now).isEmpty());

        getMfaTrustEngine().remove(DateTimeUtils.zonedDateTimeOf(record.getExpirationDate()).plusDays(1));
        getMfaTrustEngine().remove(record.getRecordKey());
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
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        GeoLocationServiceTestConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration("GeoLocationServiceTestConfiguration")
    public static class GeoLocationServiceTestConfiguration {
        @Bean
        public GeoLocationService geoLocationService() {
            val service = mock(GeoLocationService.class);
            val response = new GeoLocationResponse();
            response.addAddress("MSIE");
            when(service.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(response);
            return service;
        }
    }
}
