package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.binding.expression.Expression;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.test.MockRequestContext;
import org.springframework.webflow.test.MockRequestControlContext;

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

    @Test
    public void verifyRenderAction() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val cfg = casWebflowExecutionPlan.getWebflowConfigurers().iterator().next();
        assertNotNull(cfg.createRenderAction("ExampleRenderAction"));

        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
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
    public void verifyWebflowConfigError() {
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val stopState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR);
        val context = new MockRequestControlContext(flow);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, new RuntimeException());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                stopState.enter(context);
            }
        });
    }
}
