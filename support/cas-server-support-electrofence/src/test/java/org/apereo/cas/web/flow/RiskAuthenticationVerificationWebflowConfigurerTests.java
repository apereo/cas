package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreEventsConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.ElectronicFenceWebflowConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RiskAuthenticationVerificationWebflowConfigurerTests}.
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
@Tag("WebflowConfig")
public class RiskAuthenticationVerificationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("riskVerificationFlowRegistry")
    private FlowDefinitionRegistry riskVerificationFlowRegistry;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) riskVerificationFlowRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_RISK_AUTHENTICATION_TOKEN_CHECK);
        assertNotNull(state);
    }
}
