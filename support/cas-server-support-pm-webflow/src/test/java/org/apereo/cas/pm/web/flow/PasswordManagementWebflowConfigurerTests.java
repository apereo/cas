package org.apereo.cas.pm.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import java.util.stream.StreamSupport;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordManagementWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowConfig")
class PasswordManagementWebflowConfigurerTests {

    @TestPropertySource(properties = {
        "cas.authn.pm.reset.security-questions-enabled=false",
        "cas.authn.pm.core.enabled=false"
    })
    @ImportAutoConfiguration({
        CasPasswordManagementAutoConfiguration.class,
        CasPasswordManagementWebflowAutoConfiguration.class
    })
    @Nested
    class DisabledTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("passwordResetHandlerAdapter")
        private HandlerAdapter passwordResetHandlerAdapter;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS)
        private Action verifySecurityQuestionsAction;

        @Test
        void verifyOperation() throws Throwable {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_AUTHENTICATION_BLOCKED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_WORKSTATION);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_AUTHENTICATION_HOURS);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS);
            assertNotNull(state);

            val handler = mock(FlowHandler.class);
            when(handler.getFlowId()).thenReturn(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            assertTrue(passwordResetHandlerAdapter.supports(handler));

            val context = MockRequestContext.create(applicationContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
            assertNotNull(state);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.core.enabled=true")
    @ImportAutoConfiguration({
        CasSimpleMultifactorAuthenticationAutoConfiguration.class,
        CasPasswordManagementAutoConfiguration.class,
        CasPasswordManagementWebflowAutoConfiguration.class
    })
    class EnabledTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        private MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider;

        @Autowired
        @Qualifier("passwordResetHandlerAdapter")
        private HandlerAdapter passwordResetHandlerAdapter;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS)
        private Action verifySecurityQuestionsAction;

        @Autowired
        @Qualifier("passwordManagementMultifactorWebflowCustomizer")
        private CasMultifactorWebflowCustomizer passwordManagementMultifactorWebflowCustomizer;

        @Test
        void verifyOperation() throws Throwable {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_AUTHENTICATION_BLOCKED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_WORKSTATION);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_AUTHENTICATION_HOURS);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS);
            assertNotNull(state);

            val webflowAttributeMappings = passwordManagementMultifactorWebflowCustomizer.getWebflowAttributeMappings();
            assertTrue(webflowAttributeMappings.contains(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_QUERY));
            assertTrue(webflowAttributeMappings.contains(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_REQUEST));
            assertTrue(webflowAttributeMappings.contains(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION));
            assertTrue(webflowAttributeMappings.contains(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER));

            val handler = mock(FlowHandler.class);
            when(handler.getFlowId()).thenReturn(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            assertTrue(passwordResetHandlerAdapter.supports(handler));

            val startState = (ActionState) flow.getStartState();
            assertEquals(CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW,
                startState.getTransition(CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD).getTargetStateId());

            val context = MockRequestContext.create(applicationContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
            assertNotNull(state);

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_CHANGE);
            assertNotNull(state);

            val event = StreamSupport.stream(flow.getStartActionList().spliterator(), false)
                .filter(ConsumerExecutionAction.class::isInstance)
                .findFirst()
                .orElseThrow()
                .execute(context);
            assertNull(event);
            assertTrue(WebUtils.isPasswordManagementEnabled(context));
            assertTrue(WebUtils.isForgotUsernameEnabled(context));

            val passwordResetFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            val sendAcct = (TransitionableState) passwordResetFlow.getState(CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET);
            assertEquals(sendAcct.getTransition(casSimpleMultifactorAuthenticationProvider.getId()).getTargetStateId(),
                casSimpleMultifactorAuthenticationProvider.getId());
        }
    }
}

