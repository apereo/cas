package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLoginWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class DefaultLoginWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_POST_VIEW));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_REDIRECT));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS));
    }
}
