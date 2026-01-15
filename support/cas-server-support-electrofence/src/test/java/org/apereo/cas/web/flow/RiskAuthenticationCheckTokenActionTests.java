package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasElectronicFenceAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RiskAuthenticationCheckTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ImportAutoConfiguration({
    CasCoreEventsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasElectronicFenceAutoConfiguration.class
})
@TestPropertySource(properties = "cas.authn.adaptive.risk.ip.enabled=true")
@Tag("WebflowActions")
class RiskAuthenticationCheckTokenActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
    private Action riskCheckAction;

    @Autowired
    @Qualifier("authenticationRiskEmailNotifier")
    private AuthenticationRiskNotifier authenticationRiskEmailNotifier;

    @BeforeEach
    @Override
    void setup() {
        super.setup();
        
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("1.2.3.4");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        authenticationRiskEmailNotifier.setRegisteredService(RegisteredServiceTestUtils.getRegisteredService());
        authenticationRiskEmailNotifier.setAuthentication(RegisteredServiceTestUtils.getAuthentication());
        authenticationRiskEmailNotifier.setClientInfo(ClientInfoHolder.getClientInfo());
        authenticationRiskEmailNotifier.setAuthenticationRiskScore(AuthenticationRiskScore.lowestRiskScore());
    }

    @Test
    void verifyOperation() throws Throwable {
        val riskToken = authenticationRiskEmailNotifier.createRiskToken();
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(RiskAuthenticationCheckTokenAction.PARAMETER_NAME_RISK_TOKEN, riskToken);
        val event = riskCheckAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    void verifyMissingToken() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val event = riskCheckAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
    }
}
