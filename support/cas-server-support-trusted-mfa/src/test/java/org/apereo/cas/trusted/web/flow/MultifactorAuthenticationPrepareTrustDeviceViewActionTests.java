package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationPrepareTrustDeviceViewActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@Execution(ExecutionMode.SAME_THREAD)
class MultifactorAuthenticationPrepareTrustDeviceViewActionTests {

    @SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.trusted.core.device-registration-enabled=true",
            "cas.authn.mfa.trusted.core.auto-assign-device-name=true"
        })
    @Nested
    class AutoNamingStrategy extends AbstractMultifactorAuthenticationTrustStorageTests {
        private MockRequestContext context;

        @BeforeEach
        public void beforeEach() throws Throwable {
            context = MockRequestContext.create(applicationContext);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap()));

            context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            val request = context.getHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("123.456.789.000");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
            WebUtils.putAuthentication(authn, context);

            ApplicationContextProvider.holdApplicationContext(applicationContext);
        }

        @Test
        void verifyRegisterDevice() throws Throwable {
            val bean = new MultifactorAuthenticationTrustBean();
            WebUtils.putMultifactorAuthenticationTrustRecord(context, bean);
            assertNull(bean.getDeviceName());
            assertEquals(CasWebflowConstants.TRANSITION_ID_STORE, mfaPrepareTrustDeviceViewAction.execute(context).getId());
            assertFalse(bean.getDeviceName().isBlank());
            assertTrue(bean.getDeviceName().startsWith(ClientInfoHolder.getClientInfo().getClientIpAddress()));
        }
    }

    @SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
    @Nested
    class DefaultNamingStrategy extends AbstractMultifactorAuthenticationTrustStorageTests {
        private MockRequestContext context;
        
        @BeforeEach
        public void beforeEach() throws Exception {
            context = MockRequestContext.create(applicationContext);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.emptyMap()));

            context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            val request = context.getHttpServletRequest();
            request.setRemoteAddr("123.456.789.000");
            request.setLocalAddr("123.456.789.000");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            val record = getMultifactorAuthenticationTrustRecord();
            record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
            val deviceFingerprint = deviceFingerprintStrategy.determineFingerprintComponent(record.getPrincipal(), request, context.getHttpServletResponse());
            record.setDeviceFingerprint(deviceFingerprint);
            mfaTrustEngine.save(record);

            assertNotNull(context.getHttpServletResponse().getCookies());
            assertEquals(1, context.getHttpServletResponse().getCookies().length);
            request.setCookies(context.getHttpServletResponse().getCookies());

            val authn = RegisteredServiceTestUtils.getAuthentication(record.getPrincipal());
            WebUtils.putAuthentication(authn, context);

            ApplicationContextProvider.holdApplicationContext(applicationContext);
        }

        @Test
        void verifyRegisterDevice() throws Throwable {
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }

        @Test
        void verifyPrepWithBypass() throws Throwable {
            val service = (BaseRegisteredService) WebUtils.getRegisteredService(context);
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setBypassTrustedDeviceEnabled(true);
            service.setMultifactorAuthenticationPolicy(policy);
            WebUtils.putRegisteredService(context, service);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }

        @Test
        void verifyPrepWithNoBypassAndService() throws Throwable {
            WebUtils.putRegisteredService(context, null);
            WebUtils.putServiceIntoFlowScope(context, null);
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }
    }
}
