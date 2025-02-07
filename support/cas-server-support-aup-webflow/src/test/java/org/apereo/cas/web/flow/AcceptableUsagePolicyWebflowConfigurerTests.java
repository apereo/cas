package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ImportAutoConfiguration(CasAcceptableUsagePolicyWebflowAutoConfiguration.class)
@Tag("WebflowConfig")
class AcceptableUsagePolicyWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_AUP_CHECK));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW));
    }
}

