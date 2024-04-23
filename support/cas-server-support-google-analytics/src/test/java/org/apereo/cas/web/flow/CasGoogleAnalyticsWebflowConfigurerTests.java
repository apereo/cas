package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasGoogleAnalyticsAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasGoogleAnalyticsWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties ="cas.google-analytics.google-analytics-tracking-id=123456")
@Import(CasGoogleAnalyticsAutoConfiguration.class)
@Tag("WebflowConfig")
class CasGoogleAnalyticsWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
        assertTrue(Arrays.stream(state.getExitActionList().toArray())
            .anyMatch(ac -> ac.toString().contains(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_CREATE_COOKIE)));

        val logoutFlow = (Flow) this.logoutFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        state = (TransitionableState) logoutFlow.getState(CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        assertTrue(Arrays.stream(state.getExitActionList().toArray())
            .anyMatch(ac -> ac.toString().contains(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_REMOVE_COOKIE)));
    }
}
