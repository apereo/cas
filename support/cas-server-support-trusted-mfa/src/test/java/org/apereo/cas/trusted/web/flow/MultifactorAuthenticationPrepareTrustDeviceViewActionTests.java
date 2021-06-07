package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.HttpRequestUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

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
public class MultifactorAuthenticationPrepareTrustDeviceViewActionTests {

    @SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.trusted.core.device-registration-enabled=true",
            "cas.authn.mfa.trusted.core.auto-assign-device-name=true"
        })
    @Nested
    @Tag("WebflowMfaActions")
    @SuppressWarnings("ClassCanBeStatic")
    public class AutoNamingStrategy extends AbstractMultifactorAuthenticationTrustStorageTests {
        private MockRequestContext context;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            this.context = new MockRequestContext();
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.EMPTY_MAP));

            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("123.456.789.000");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            
            val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
            WebUtils.putAuthentication(authn, context);

            ApplicationContextProvider.holdApplicationContext(applicationContext);
        }

        @Test
        public void verifyRegisterDevice() throws Exception {
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
    @Tag("WebflowMfaActions")
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultNamingStrategy extends AbstractMultifactorAuthenticationTrustStorageTests {
        private MockRequestContext context;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @BeforeEach
        public void beforeEach() {
            this.context = new MockRequestContext();
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.EMPTY_MAP));

            val request = new MockHttpServletRequest();
            request.setRemoteAddr("123.456.789.000");
            request.setLocalAddr("123.456.789.000");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val record = getMultifactorAuthenticationTrustRecord();
            record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(5));
            val deviceFingerprint = deviceFingerprintStrategy.determineFingerprintComponent(record.getPrincipal(), request, response);
            record.setDeviceFingerprint(deviceFingerprint);
            mfaTrustEngine.save(record);

            assertNotNull(response.getCookies());
            assertEquals(response.getCookies().length, 1);
            request.setCookies(response.getCookies());

            val authn = RegisteredServiceTestUtils.getAuthentication(record.getPrincipal());
            WebUtils.putAuthentication(authn, context);

            ApplicationContextProvider.holdApplicationContext(applicationContext);
        }

        @Test
        public void verifyRegisterDevice() throws Exception {
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }

        @Test
        public void verifyPrepWithBypass() throws Exception {
            val service = (AbstractRegisteredService) WebUtils.getRegisteredService(context);
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setBypassTrustedDeviceEnabled(true);
            service.setMultifactorPolicy(policy);
            WebUtils.putRegisteredService(context, service);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }

        @Test
        public void verifyPrepWithNoBypassAndService() throws Exception {
            WebUtils.putRegisteredService(context, null);
            WebUtils.putServiceIntoFlowScope(context, null);
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER,
                mfaPrepareTrustDeviceViewAction.execute(context).getId());
        }
    }
}
