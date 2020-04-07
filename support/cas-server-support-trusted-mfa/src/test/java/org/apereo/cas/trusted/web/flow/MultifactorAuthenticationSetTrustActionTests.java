package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationSetTrustActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Getter
@Tag("Webflow")
public class MultifactorAuthenticationSetTrustActionTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    private MockRequestContext context;

    private MockHttpServletRequest request;

    @BeforeEach
    public void beforeEach() {
        this.context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("sample-service", Collections.EMPTY_MAP);
        WebUtils.putRegisteredService(context, registeredService);

        request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser-setdevice");
        WebUtils.putAuthentication(authn, context);
    }

    @Test
    public void verifySetDevice() throws Exception {
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        WebUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.getAttributes().containsKey(
            casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute()));
    }

    @Test
    public void verifyNoAuthN() throws Exception {
        WebUtils.putAuthentication(null, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, mfaSetTrustAction.execute(context).getId());
    }

    @Test
    public void verifyBypass() throws Exception {
        val service = (AbstractRegisteredService) WebUtils.getRegisteredService(context);
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassTrustedDeviceEnabled(true);
        service.setMultifactorPolicy(policy);
        WebUtils.putRegisteredService(context, service);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            mfaSetTrustAction.execute(context).getId());
    }

    @Test
    public void verifyNoDeviceName() throws Exception {
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val record = mfaTrustEngine.get("casuser-setdevice");
        assertTrue(record.isEmpty());
    }

    @Test
    public void verifyDeviceTracked() throws Exception {
        MultifactorAuthenticationTrustUtils.setMultifactorAuthenticationTrustedInScope(this.context);
        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        WebUtils.putMultifactorAuthenticationTrustRecord(context, bean);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());
        val record = mfaTrustEngine.get("casuser-setdevice");
        assertTrue(record.isEmpty());
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.getAttributes().containsKey(
            casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute()));
    }
}
