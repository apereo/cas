package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationTrustedDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMultifactorAuthenticationTrustedDeviceProviderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
public class DefaultMultifactorAuthenticationTrustedDeviceProviderActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)
    protected Action mfaSetTrustAction;

    @Autowired
    @Qualifier("multifactorAuthenticationTrustedDeviceProviderAction")
    private MultifactorAuthenticationTrustedDeviceProviderAction action;

    @Test
    void verifyOperation() throws Throwable {
        val context = getMockRequestContext();

        val bean = new MultifactorAuthenticationTrustBean().setDeviceName("ApereoCAS");
        WebUtils.putMultifactorAuthenticationTrustRecord(context, bean);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mfaSetTrustAction.execute(context).getId());

        val result = action.execute(context);
        assertNull(result);
        assertEquals(1, WebUtils.getMultifactorAuthenticationTrustedDevices(context).size());
    }

    private static MockRequestContext getMockRequestContext() throws Throwable {
        val context = MockRequestContext.create();
        context.getHttpServletRequest().setRemoteAddr("123.456.789.123");
        context.getHttpServletRequest().setLocalAddr("123.456.789.123");
        context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authn, context);
        return context;
    }
}
