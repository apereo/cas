package org.apereo.cas.pm.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.config.PasswordManagementForgotUsernameConfiguration;
import org.apereo.cas.config.PasswordManagementWebflowConfiguration;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
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
    @Import({
        PasswordManagementConfiguration.class,
        PasswordManagementWebflowConfiguration.class,
        PasswordManagementForgotUsernameConfiguration.class
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
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
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

            val context = MockRequestContext.create();
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
            assertNotNull(state);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.core.enabled=true")
    @Import({
        CasSimpleMultifactorAuthenticationConfiguration.class,
        CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
        CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration.class,

        PasswordManagementConfiguration.class,
        PasswordManagementWebflowConfiguration.class,
        PasswordManagementForgotUsernameConfiguration.class
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
            val flow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
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

            assertTrue(passwordManagementMultifactorWebflowCustomizer.getWebflowAttributeMappings()
                .contains(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_QUERY));

            val handler = mock(FlowHandler.class);
            when(handler.getFlowId()).thenReturn(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
            assertTrue(passwordResetHandlerAdapter.supports(handler));

            val startState = (ActionState) flow.getStartState();
            assertEquals(CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS,
                startState.getTransition(CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD).getTargetStateId());

            val sendAcct = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);
            assertEquals(sendAcct.getTransition(casSimpleMultifactorAuthenticationProvider.getId()).getTargetStateId(),
                casSimpleMultifactorAuthenticationProvider.getId());

            val context = MockRequestContext.create();
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
            assertNotNull(state);
            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
            assertNotNull(state);

            state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION);
            assertNotNull(state);

            val event = StreamSupport.stream(flow.getStartActionList().spliterator(), false)
                .filter(ConsumerExecutionAction.class::isInstance)
                .findFirst()
                .orElseThrow()
                .execute(context);
            assertNull(event);
            assertTrue(WebUtils.isPasswordManagementEnabled(context));
        }
    }
}

