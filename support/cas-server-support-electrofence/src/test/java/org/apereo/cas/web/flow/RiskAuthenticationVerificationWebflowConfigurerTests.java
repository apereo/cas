package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasElectronicFenceAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RiskAuthenticationVerificationWebflowConfigurerTests}.
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
@Tag("WebflowConfig")
class RiskAuthenticationVerificationWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_RISK_AUTHENTICATION_TOKEN_CHECK);
        assertNotNull(state);
    }
}
