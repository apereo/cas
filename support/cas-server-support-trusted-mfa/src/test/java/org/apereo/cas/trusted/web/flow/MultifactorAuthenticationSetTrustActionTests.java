package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationSetTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class MultifactorAuthenticationSetTrustActionTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifySetDeviceWithNoName() throws Throwable {
        val context = getMockRequestContext();
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName(StringUtils.EMPTY);
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
    }

    @Test
    void verifySetDevice() throws Throwable {
        val context = getMockRequestContext();
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.containsAttribute(
            casProperties.getAuthn().getMfa().getTrusted().getCore().getAuthenticationContextAttribute()));
    }

    @Test
    void verifySetDeviceWithExp() throws Throwable {
        val context = getMockRequestContext();
        val bean = new MultifactorAuthenticationTrustBean()
            .setTimeUnit(ChronoUnit.MONTHS)
            .setExpiration(2)
            .setDeviceName("ApereoCAS-Device");
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.containsAttribute(
            casProperties.getAuthn().getMfa().getTrusted().getCore().getAuthenticationContextAttribute()));
    }


    @Test
    void verifyNoAuthN() throws Throwable {
        val context = getMockRequestContext();
        WebUtils.putAuthentication((Authentication) null, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, mfaSetTrustAction.execute(context).getId());
    }

    @Test
    void verifyBypass() throws Throwable {
        val context = getMockRequestContext();
        val service = (BaseRegisteredService) WebUtils.getRegisteredService(context);
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassTrustedDeviceEnabled(true);
        service.setMultifactorAuthenticationPolicy(policy);
        WebUtils.putRegisteredService(context, service);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            mfaSetTrustAction.execute(context).getId());
    }

    @Test
    void verifyNoDeviceName() throws Throwable {
        val context = getMockRequestContext();
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val record = mfaTrustEngine.get("casuser-setdevice");
        assertTrue(record.isEmpty());
    }

    @Test
    void verifyDeviceTracked() throws Throwable {
        val context = getMockRequestContext();
        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(context);
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustRecord(context, bean);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val record = mfaTrustEngine.get("casuser-setdevice");
        assertTrue(record.isEmpty());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.containsAttribute(
            casProperties.getAuthn().getMfa().getTrusted().getCore().getAuthenticationContextAttribute()));
    }

    private MockRequestContext getMockRequestContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap());
        WebUtils.putRegisteredService(context, registeredService);

        context.setRemoteAddr("123.456.789.000");
        context.setLocalAddr("123.456.789.000");
        context.withUserAgent();
        context.setClientInfo();
        
        val authn = RegisteredServiceTestUtils.getAuthentication("casuser-setdevice");
        WebUtils.putAuthentication(authn, context);
        return context;
    }
}
