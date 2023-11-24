package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationVerifyTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("WebflowMfaActions")
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

        context.getHttpServletRequest().setRemoteAddr("123.456.789.000");
        context.getHttpServletRequest().setLocalAddr("123.456.789.000");
        context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

        val record = getMultifactorAuthenticationTrustRecord();
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        val deviceFingerprint = deviceFingerprintStrategy.determineFingerprintComponent(record.getPrincipal(),
            context.getHttpServletRequest(), context.getHttpServletResponse());
        record.setDeviceFingerprint(deviceFingerprint);
        mfaTrustEngine.save(record);

        assertNotNull(context.getHttpServletResponse().getCookies());
        assertEquals(1, context.getHttpServletResponse().getCookies().length);
        context.setRequestCookiesFromResponse();

        val authn = RegisteredServiceTestUtils.getAuthentication(record.getPrincipal());
        WebUtils.putAuthentication(authn, context);
        assertEquals("yes", mfaVerifyTrustAction.execute(context).getId());

        assertTrue(MultifactorAuthenticationTrustUtils.isMultifactorAuthenticationTrustedInScope(context));
        assertTrue(authn.containsAttribute(casProperties.getAuthn().getMfa().getTrusted().getCore().getAuthenticationContextAttribute()));
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
}
