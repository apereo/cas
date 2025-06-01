package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.CasLocaleChangeInterceptor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.Expression;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultLoginWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.view.custom-login-form-fields.field1.required=false")
class DefaultLoginWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val interceptors = casWebflowExecutionPlan.getWebflowInterceptors();
        assertEquals(2, interceptors.size());
        assertTrue(interceptors.stream().anyMatch(CasLocaleChangeInterceptor.class::isInstance));
        assertTrue(interceptors.stream().anyMatch(ResourceUrlProviderExposingInterceptor.class::isInstance));
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_POST_VIEW));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_REDIRECT));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SERVICE_AUTHZ));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS));
    }

    @Test
    void verifyRenderAction() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val cfg = casWebflowExecutionPlan.getWebflowConfigurers().iterator().next();
        assertNotNull(cfg.createRenderAction("ExampleRenderAction"));

        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        assertNull(cfg.getState(flow, "InvalidStateId", ViewState.class));
        assertNull(cfg.createTransitionForState(null, "BadCriteria",
            "BadTarget", false, Map.of(), (Action) null));
        assertNull(cfg.createTransitionForState(null, "BadCriteria",
            "BadTarget", Map.of(), (Action) null));
        assertTrue(cfg.getTransitionExecutionCriteriaChainForTransition(mock(Transition.class)).isEmpty());
        assertNull(cfg.createViewState(null, "ViewState", (ViewFactory) null));
        assertNull(cfg.createViewState(null, "ViewState", (Expression) null, null));
        assertNotNull(cfg.createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, (ViewFactory) null));
    }

    @Test
    void verifyWebflowConfigError() throws Throwable {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val stopState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR);
        val context = MockRequestContext.create(applicationContext);
        context.setActiveFlow(flow);
        context.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, new RuntimeException());
        assertDoesNotThrow(() -> stopState.enter(context));
    }

    @Test
    void verifyStorageStates() {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val writeState = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_BROWSER_STORAGE_WRITE);
        assertNotNull(writeState);
        assertEquals(1, writeState.getEntryActionList().size());
        assertEquals(CasWebflowConstants.STATE_ID_SERVICE_CHECK, writeState.getTransition(CasWebflowConstants.TRANSITION_ID_CONTINUE).getTargetStateId());
        val readState = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ);
        assertNotNull(readState);
        assertEquals(1, readState.getRenderActionList().size());
        assertEquals(0, readState.getEntryActionList().size());
        assertEquals(CasWebflowConstants.STATE_ID_VERIFY_BROWSER_STORAGE_READ, readState.getTransition(CasWebflowConstants.TRANSITION_ID_CONTINUE).getTargetStateId());
    }
}
