package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLogoutWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class DefaultLogoutWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.logoutFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_TERMINATE_SESSION));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISH_LOGOUT));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_LOGOUT_VIEW));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FRONT_LOGOUT));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DO_LOGOUT));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW));
    }
}
