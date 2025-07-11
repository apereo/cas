package org.apereo.cas.trusted;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
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
@ExtendWith(CasTestExtension.class)
public abstract class AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
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

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    protected static MultifactorAuthenticationTrustRecord getMultifactorAuthenticationTrustRecord() {
        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint(UUID.randomUUID().toString());
        record.setName("DeviceName");
        record.setPrincipal(UUID.randomUUID().toString());
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        record.setExpirationDate(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1)));
        return record;
    }

    @Test
    void verifyTrustEngine() throws Throwable {
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
        assertNull(getMfaTrustEngine().get(record.getId()));

        if (mfaTrustEngine instanceof final DisposableBean disposableBean) {
            disposableBean.destroy();
        }
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasMultifactorAuthnTrustAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import({CasRegisteredServicesTestConfiguration.class, GeoLocationServiceTestConfiguration.class})
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "GeoLocationServiceTestConfiguration", proxyBeanMethods = false)
    public static class GeoLocationServiceTestConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public GeoLocationService geoLocationService() throws Throwable {
            val service = mock(GeoLocationService.class);
            val response = new GeoLocationResponse();
            response.addAddress("MSIE");
            when(service.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(response);
            return service;
        }
    }

    @TestConfiguration(value = "TestMultifactorProviderTestConfiguration", proxyBeanMethods = false)
    public static class TestMultifactorProviderTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
