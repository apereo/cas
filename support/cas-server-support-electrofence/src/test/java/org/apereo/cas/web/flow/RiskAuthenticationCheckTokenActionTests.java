package org.apereo.cas.web.flow;

import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.config.CasCoreEventsConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.ElectronicFenceWebflowConfiguration;
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
import org.springframework.context.annotation.Import;
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
@Import({
    CasCoreEventsConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    ElectronicFenceConfiguration.class,
    ElectronicFenceWebflowConfiguration.class
})
@TestPropertySource(properties = "cas.authn.adaptive.risk.ip.enabled=true")
@Tag("WebflowActions")
public class RiskAuthenticationCheckTokenActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
    private Action riskCheckAction;

    @Autowired
    @Qualifier("authenticationRiskEmailNotifier")
    private AuthenticationRiskNotifier authenticationRiskEmailNotifier;

    @BeforeEach
    public void setup() {
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
    void verifyOperation() throws Exception {
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
