package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasAcceptableUsagePolicyWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class AcceptableUsagePolicyWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_AUP_CHECK));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW));
    }
}

