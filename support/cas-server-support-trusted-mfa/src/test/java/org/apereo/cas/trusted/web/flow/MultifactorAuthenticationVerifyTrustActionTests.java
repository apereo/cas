package org.apereo.cas.trusted.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class MultifactorAuthenticationVerifyTrustActionTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyDeviceNotTrusted() throws Throwable {
        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        getMfaTrustEngine().save(record);

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(record.getPrincipal()), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, mfaVerifyTrustAction.execute(context).getId());
    }

    @Test
    void verifyDeviceTrusted() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap()));

        context.setRemoteAddr("123.456.789.000");
        context.setLocalAddr("123.456.789.000");
        context.withUserAgent();
        context.setClientInfo();

        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        val deviceFingerprint = deviceFingerprintStrategy.determineFingerprint(
            RegisteredServiceTestUtils.getAuthentication(record.getPrincipal()),
            context.getHttpServletRequest(), context.getHttpServletResponse());
        record.setDeviceFingerprint(deviceFingerprint);
        mfaTrustEngine.save(record);

        assertNotNull(context.getHttpServletResponse().getCookies());
        assertEquals(1, context.getHttpServletResponse().getCookies().length);
        context.setRequestCookiesFromResponse();

        val authn = RegisteredServiceTestUtils.getAuthentication(record.getPrincipal());
        WebUtils.putAuthentication(authn, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, mfaVerifyTrustAction.execute(context).getId());
        assertTrue(MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(context));
        assertTrue(authn.containsAttribute(casProperties.getAuthn().getMfa().getTrusted().getCore().getAuthenticationContextAttribute()));
        assertTrue(MultifactorAuthenticationTrustUtils.getMultifactorAuthenticationTrustRecord(context, MultifactorAuthenticationTrustRecord.class).isPresent());
    }

    @Test
    void verifySkipVerify() throws Throwable {
        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, mfaVerifyTrustAction.execute(context).getId());

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("bad-principal"), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap());
        registeredService.setMultifactorAuthenticationPolicy(new DefaultRegisteredServiceMultifactorPolicy().setBypassTrustedDeviceEnabled(true));
        WebUtils.putRegisteredService(context, registeredService);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, mfaVerifyTrustAction.execute(context).getId());

        registeredService.setMultifactorAuthenticationPolicy(new DefaultRegisteredServiceMultifactorPolicy());
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, mfaVerifyTrustAction.execute(context).getId());
    }

    @Test
    void verifyTrustedDeviceDisabledForFlow() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap());
        WebUtils.putRegisteredService(context, registeredService);
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustedDevicesDisabled(context, Boolean.TRUE);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, mfaVerifyTrustAction.execute(context).getId());
    }

    @Test
    void verifyTrustedDeviceDisabledForPublicWorkstations() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION, Boolean.TRUE.toString());
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap());
        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, mfaVerifyTrustAction.execute(context).getId());
    }
}
