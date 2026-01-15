package org.apereo.cas.web.flow.configurer;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionCriteriaChain;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.servlet.ServletMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowConfig")
class CasWebflowConfigurerTests {
    @Test
    void verifyNoAutoConfig() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val props = new CasConfigurationProperties();
        props.getWebflow().getAutoConfiguration().setEnabled(false);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            mock(FlowDefinitionRegistry.class), applicationContext, props) {
        };
        assertDoesNotThrow(cfg::initialize);
        assertDoesNotThrow(() -> cfg.postInitialization(applicationContext));
        assertNotNull(cfg.getName());
    }

    @Test
    void verifyFailAutoConfig() {
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            mock(FlowDefinitionRegistry.class), new StaticApplicationContext(), new CasConfigurationProperties()) {
            @Override
            protected void doInitialize() {
                throw new IllegalArgumentException("failure");
            }
        };
        assertDoesNotThrow(cfg::initialize);
    }

    @Test
    void verifyMissingFlow() {
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            null, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        assertNull(cfg.getLoginFlow());
        assertNull(cfg.getLogoutFlow());
    }

    @Test
    void verifyNoLoginFlow() {
        val registry = mock(FlowDefinitionRegistry.class);
        when(registry.getFlowDefinitionIds()).thenReturn(ArrayUtils.EMPTY_STRING_ARRAY);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        assertNull(cfg.getLoginFlow());
    }

    @Test
    void verifyTransition() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(TransitionableState.class);
        when(state.getId()).thenReturn("example");
        when(state.getTransitionSet()).thenReturn(new TransitionSet());
        val transition = cfg.createTransition("destination", state);
        assertNotNull(transition);

        val transition2 = cfg.createTransitionForState(state, "criteria");
        assertNotNull(transition2);

        val flow = mock(Flow.class);
        assertNull(cfg.getTransitionableState(flow, "example", ActionState.class));
        assertNull(cfg.getTransitionableState(flow, "example"));
    }

    @Test
    void verifyNoEvalAction() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(null,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val transition = cfg.createEvaluateAction("exampleAction");
        assertNull(transition);
    }

    @Test
    void verifyDuplicateDecisionState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        val state = mock(DecisionState.class);
        when(flow.containsState("decisionId")).thenReturn(Boolean.TRUE);
        when(flow.getTransitionableState("decisionId")).thenReturn(state);
        val transition = cfg.createDecisionState(flow, "decisionId", "true", "trueState", "elseState");
        assertNotNull(transition);
    }

    @Test
    void verifyDuplicateEndState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        val state = mock(EndState.class);
        when(flow.containsState("endStateId")).thenReturn(Boolean.TRUE);
        when(flow.getStateInstance("endStateId")).thenReturn(state);
        val endState = cfg.createEndState(flow, "endStateId", mock(ViewFactory.class));
        assertNotNull(endState);
    }

    @Test
    void verifyDuplicateViewState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        val state = mock(ViewState.class);
        when(flow.containsState("viewStateId")).thenReturn(Boolean.TRUE);
        when(flow.getTransitionableState("viewStateId")).thenReturn(state);
        val viewState = cfg.createViewState(flow, "viewStateId",
            mock(Expression.class), mock(BinderConfiguration.class));
        assertNotNull(viewState);
    }

    @Test
    void verifySubflowState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        when(flow.containsState("SubflowState")).thenReturn(Boolean.FALSE);
        val subState = cfg.createSubflowState(flow, "SubflowState", "SubflowState", mock(Action.class));
        assertNotNull(subState);
        assertNotEquals(0, subState.getEntryActionList().size());
    }

    @Test
    void verifyDuplicateSubflowState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        val state = mock(SubflowState.class);
        when(flow.containsState("SubflowState")).thenReturn(Boolean.TRUE);
        when(flow.getTransitionableState("SubflowState")).thenReturn(state);
        val subState = cfg.createSubflowState(flow, "SubflowState", "SubflowState", mock(Action.class));
        assertNotNull(subState);
    }

    @Test
    void verifyRedirectEndState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val services = mock(FlowBuilderServices.class);
        val viewFactory = mock(ViewFactory.class);
        val creator = mock(ViewFactoryCreator.class);
        when(creator.createViewFactory(any(), any(), any(), any(), any(), any()))
            .thenReturn(viewFactory);
        when(services.getViewFactoryCreator()).thenReturn(creator);
        val cfg = new AbstractCasWebflowConfigurer(services,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        val state = mock(EndState.class);
        when(flow.containsState("endStateId")).thenReturn(Boolean.TRUE);
        when(flow.getStateInstance("endStateId")).thenReturn(state);
        val endState = cfg.createEndState(flow, "endStateId", "viewId", false);
        assertNotNull(endState);
    }

    @Test
    void verifyDefaultTransition() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(TransitionableState.class);
        when(state.getId()).thenReturn("example");
        assertDoesNotThrow(() -> cfg.createStateDefaultTransition(null, "target"));
        assertDoesNotThrow(() -> cfg.createStateDefaultTransition(null, state));
    }

    @Test
    void verifyMapping() {
        val registry = mock(FlowDefinitionRegistry.class);
        val fbs = mock(FlowBuilderServices.class);
        val parser = mock(ExpressionParser.class);
        when(parser.parseExpression(anyString(), any())).thenReturn(mock(Expression.class));
        when(fbs.getExpressionParser()).thenReturn(parser);
        when(fbs.getConversionService()).thenReturn(mock(ConversionService.class));
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(TransitionableState.class);
        when(state.getId()).thenReturn("example");
        assertNotNull(cfg.createFlowMapping("source", "target", false, Boolean.class));
    }

    @Test
    void verifyContains() {
        val registry = mock(FlowDefinitionRegistry.class);
        val fbs = mock(FlowBuilderServices.class);
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        assertFalse(cfg.containsFlowState(null, "id"));
        assertFalse(cfg.containsSubflowState(null, "id"));
        assertFalse(cfg.containsTransition(null, "id"));
    }

    @Test
    void verifyViewBinder() {
        val registry = mock(FlowDefinitionRegistry.class);
        val fbs = mock(FlowBuilderServices.class);
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(ViewState.class);
        when(state.getViewFactory()).thenReturn(new ServletMvcViewFactory(mock(Expression.class), mock(FlowViewResolver.class),
            mock(ExpressionParser.class), mock(ConversionService.class),
            mock(BinderConfiguration.class), mock(MessageCodesResolver.class)));
        assertNotNull(cfg.getViewStateBinderConfiguration(state));
    }

    @Test
    void verifyCriteria() {
        val registry = mock(FlowDefinitionRegistry.class);
        val fbs = mock(FlowBuilderServices.class);
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val transition = mock(Transition.class);
        val criteria = new TransitionCriteriaChain();
        when(transition.getExecutionCriteria()).thenReturn(criteria);
        assertTrue(cfg.getTransitionExecutionCriteriaChainForTransition(transition).isEmpty());

        when(transition.getExecutionCriteria()).thenReturn(mock(TransitionCriteria.class));
        assertFalse(cfg.getTransitionExecutionCriteriaChainForTransition(transition).isEmpty());
    }

    @Test
    void verifyExpression() {
        val registry = mock(FlowDefinitionRegistry.class);
        val fbs = mock(FlowBuilderServices.class);
        when(fbs.getConversionService()).thenReturn(mock(ConversionService.class));
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val action = new EvaluateAction(cfg.createExpression("example"), cfg.createExpression("result"));
        assertNotNull(cfg.getExpressionStringFromAction(action));
    }

    @Test
    void verifyTransitionCreate() {
        val registry = mock(FlowDefinitionRegistry.class);

        val fbs = mock(FlowBuilderServices.class);
        when(fbs.getConversionService()).thenReturn(mock(ConversionService.class));
        val cfg = new AbstractCasWebflowConfigurer(fbs,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };

        val flow = mock(Flow.class);
        val state = mock(TransitionableState.class);
        when(flow.containsState("endStateId")).thenReturn(Boolean.TRUE);
        when(flow.getState("endStateId")).thenReturn(state);
        assertDoesNotThrow(() -> cfg.createTransitionsForState(flow, "endStateId", Map.of()));
    }
}


