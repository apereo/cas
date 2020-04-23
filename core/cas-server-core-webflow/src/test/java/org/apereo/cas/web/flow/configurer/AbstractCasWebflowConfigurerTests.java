package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractCasWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class AbstractCasWebflowConfigurerTests {
    @Test
    public void verifyNoAutoConfig() {
        val props = new CasConfigurationProperties();
        props.getWebflow().setAutoconfigure(false);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            mock(FlowDefinitionRegistry.class), new StaticApplicationContext(), props) {
        };
        assertDoesNotThrow(cfg::initialize);
    }

    @Test
    public void verifyFailAutoConfig() {
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
    public void verifyMissingFlow() {
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            null, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        assertNull(cfg.getLoginFlow());
        assertNull(cfg.getLogoutFlow());
    }

    @Test
    public void verifyNoLoginFlow() {
        val registry = mock(FlowDefinitionRegistry.class);
        when(registry.getFlowDefinitionIds()).thenReturn(ArrayUtils.EMPTY_STRING_ARRAY);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        assertNull(cfg.getLoginFlow());
    }

    @Test
    public void verifyTransition() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(TransitionableState.class);
        when(state.getId()).thenReturn("example");
        val transition = cfg.createTransition("destination", state);
        assertNotNull(transition);
    }

    @Test
    public void verifyNoEvalAction() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(null,
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val transition = cfg.createEvaluateAction("exampleAction");
        assertNull(transition);
    }

    @Test
    public void verifyDuplicateDecisionState() {
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
    public void verifyDuplicateEndState() {
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
    public void verifyDuplicateViewState() {
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
    public void verifySubflowState() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val flow = mock(Flow.class);
        when(flow.containsState("SubflowState")).thenReturn(Boolean.FALSE);
        val subState = cfg.createSubflowState(flow, "SubflowState", "SubflowState", mock(Action.class));
        assertNotNull(subState);
        assertFalse(subState.getEntryActionList().size() == 0);
    }

    @Test
    public void verifyDuplicateSubflowState() {
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
    public void verifyRedirectEndState() {
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
    public void verifyDefaultTransition() {
        val registry = mock(FlowDefinitionRegistry.class);
        val cfg = new AbstractCasWebflowConfigurer(mock(FlowBuilderServices.class),
            registry, new StaticApplicationContext(), new CasConfigurationProperties()) {
        };
        val state = mock(TransitionableState.class);
        when(state.getId()).thenReturn("example");
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                cfg.createStateDefaultTransition(null, "target");
            }
        });
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                cfg.createStateDefaultTransition(null, state);
            }
        });
    }

    @Test
    public void verifyMapping() {
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
        assertNotNull(cfg.createMappingToSubflowState("target", "source", false, Boolean.class));
    }

    @Test
    public void verifyContains() {
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
    public void verifyViewBinder() {
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
    public void verifyCriteria() {
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
    public void verifyExpression() {
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
    public void verifyTransitionCreate() {
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
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                cfg.createTransitionsForState(flow, "endStateId", Map.of());
            }
        });
    }
}


