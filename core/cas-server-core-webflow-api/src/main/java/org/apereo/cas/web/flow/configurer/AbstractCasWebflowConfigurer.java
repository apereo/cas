package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.states.EndViewState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.convert.service.RuntimeBindingConversionExecutor;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.spel.SpringELExpressionParser;
import org.springframework.binding.expression.support.FluentParserContext;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.binding.mapping.impl.DefaultMappingContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.action.RenderAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.action.ViewFactoryActionAdapter;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.SubflowAttributeMapper;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.WildcardTransitionCriteria;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.model.FlowModelFlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.model.builder.DefaultFlowModelHolder;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;
import org.springframework.webflow.engine.support.ActionTransitionCriteria;
import org.springframework.webflow.engine.support.BeanFactoryVariableValueFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.engine.support.GenericSubflowAttributeMapper;
import org.springframework.webflow.engine.support.TransitionCriteriaChain;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.expression.spel.ActionPropertyAccessor;
import org.springframework.webflow.expression.spel.BeanFactoryPropertyAccessor;
import org.springframework.webflow.expression.spel.FlowVariablePropertyAccessor;
import org.springframework.webflow.expression.spel.MapAdaptablePropertyAccessor;
import org.springframework.webflow.expression.spel.MessageSourcePropertyAccessor;
import org.springframework.webflow.expression.spel.ScopeSearchingPropertyAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The {@link AbstractCasWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = "name")
public abstract class AbstractCasWebflowConfigurer implements CasWebflowConfigurer {


    /**
     * Flow builder services.
     */
    protected final FlowBuilderServices flowBuilderServices;

    /**
     * The main CAS flow definition registry.
     */
    protected final FlowDefinitionRegistry flowDefinitionRegistry;

    /**
     * Application context.
     */
    protected final ConfigurableApplicationContext applicationContext;

    /**
     * CAS Properties.
     */
    protected final CasConfigurationProperties casProperties;

    private int order;

    private String name = getClass().getSimpleName();

    private static TransitionCriteria getTransitionCriteriaForExpression(final Expression criteriaOutcomeExpression) {
        if (criteriaOutcomeExpression.toString().equals(WildcardTransitionCriteria.WILDCARD_EVENT_ID)) {
            return WildcardTransitionCriteria.INSTANCE;
        }
        return new DefaultTransitionCriteria(criteriaOutcomeExpression);
    }

    @Override
    public void initialize() {
        FunctionUtils.doAndHandle(o -> {
            LOGGER.trace("Initializing CAS webflow configuration...");
            if (casProperties.getWebflow().getAutoConfiguration().isEnabled()) {
                doInitialize();
            } else {
                LOGGER.info("Webflow auto-configuration is disabled for [{}]", getClass().getName());
            }
        }, throwable -> null).accept(this);
    }

    @Override
    public Flow getLoginFlow() {
        return getFlow(FLOW_ID_LOGIN);
    }

    @Override
    public Flow getLogoutFlow() {
        return getFlow(FLOW_ID_LOGOUT);
    }

    @Override
    public TransitionableState getStartState(final Flow flow) {
        return (TransitionableState) flow.getStartState();
    }

    @Override
    public Transition createTransition(final String criteriaOutcome, final String targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState);
    }

    @Override
    public Transition createTransition(final String criteriaOutcome, final String targetState, final Action... actions) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState, actions);
    }

    @Override
    public Transition createTransition(final String criteriaOutcome, final TransitionableState targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState.getId());
    }

    @Override
    public Transition createTransition(final Expression criteriaOutcomeExpression,
                                       final String targetState, final Action... actions) {
        return createTransition(criteriaOutcomeExpression,
            StringUtils.isNotBlank(targetState) ? new DefaultTargetStateResolver(targetState) : null,
            actions);
    }

    @Override
    public Transition createTransition(final String criteriaOutcomeExpression,
                                       final TargetStateResolver targetStateResolver) {
        return createTransition(new LiteralExpression(criteriaOutcomeExpression), targetStateResolver, EMPTY_ACTIONS_ARRAY);
    }

    @Override
    public Transition createTransition(final Expression criteriaOutcomeExpression,
                                       final TargetStateResolver targetStateResolver) {
        return createTransition(criteriaOutcomeExpression, targetStateResolver, EMPTY_ACTIONS_ARRAY);
    }

    @Override
    public Transition createTransition(final Expression criteriaOutcomeExpression,
                                       final TargetStateResolver targetStateResolver,
                                       final Action... actions) {
        val criteria = getTransitionCriteriaForExpression(criteriaOutcomeExpression);
        val transition = new Transition(criteria, targetStateResolver);
        if (actions != null && actions.length > 0) {
            val transitionActionCriteria = Arrays.stream(actions)
                .map(ActionTransitionCriteria::new)
                .toArray(ActionTransitionCriteria[]::new);
            transition.setExecutionCriteria(new TransitionCriteriaChain(transitionActionCriteria));
        }
        return transition;
    }

    @Override
    public Transition createTransition(final String targetState) {
        val resolver = new DefaultTargetStateResolver(targetState);
        return new Transition(resolver);
    }

    @Override
    public RenderAction createRenderAction(final String... fragmentExpression) {
        val ctx = new FluentParserContext();
        val expressionParser = this.flowBuilderServices.getExpressionParser();
        val expressions = Arrays.stream(fragmentExpression)
            .map(fg -> expressionParser.parseExpression(fg, ctx))
            .toArray(Expression[]::new);

        val newAction = new RenderAction(expressions);
        LOGGER.trace("Created render action for expressions [{}]", Arrays.toString(fragmentExpression));
        return newAction;
    }

    @Override
    public SetAction createSetAction(final String name, final String value) {
        return new SetAction(createExpression(name), createExpression(value));
    }

    @Override
    public EvaluateAction createEvaluateAction(final String expression) {
        if (this.flowBuilderServices == null) {
            LOGGER.warn("Flow builder services is not configured correctly.");
            return null;
        }
        val ctx = new FluentParserContext();
        val action = flowBuilderServices.getExpressionParser().parseExpression(expression, ctx);
        val newAction = new EvaluateAction(action, null);
        LOGGER.trace("Created evaluate action for expression [{}]", action.getExpressionString());
        return newAction;
    }

    @Override
    public ActionState createActionState(final Flow flow, final String name) {
        return createActionState(flow, name, EMPTY_ACTIONS_ARRAY);
    }

    @Override
    public ActionState createActionState(final Flow flow, final String name, final String... action) {
        val actionList = Arrays.stream(action).map(this::createEvaluateAction).toList();
        return createActionState(flow, name, actionList.toArray(Action[]::new));
    }

    @Override
    public ActionState createActionState(final Flow flow, final String name, final Action... actions) {
        if (containsFlowState(flow, name)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), name);
            return getTransitionableState(flow, name, ActionState.class);
        }
        val actionState = new ActionState(flow, name);
        LOGGER.trace("Created action state [{}]", actionState.getId());
        actionState.getActionList().addAll(actions);
        LOGGER.trace("Added action to the action state [{}] list of actions: [{}]",
            actionState.getId(), actionState.getActionList());
        return actionState;
    }

    @Override
    public ActionState createActionState(final Flow flow, final String name, final Action action) {
        return createActionState(flow, name, new Action[]{action});
    }

    @Override
    public DecisionState createDecisionState(final Flow flow, final String id, final String testExpression,
                                             final String thenStateId, final String elseStateId) {
        if (containsFlowState(flow, id)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return getTransitionableState(flow, id, DecisionState.class);
        }
        val decisionState = new DecisionState(flow, id);
        val expression = createExpression(testExpression, Boolean.class);
        val thenTransition = createTransition(expression, thenStateId);
        decisionState.getTransitionSet().add(thenTransition);
        val elseTransition = createTransition("*", elseStateId);
        decisionState.getTransitionSet().add(elseTransition);
        return decisionState;
    }

    @Override
    public void setStartState(final Flow flow, final String state) {
        flow.setStartState(state);
        val startState = getStartState(flow);
        LOGGER.trace("Start state is now set to [{}]", startState.getId());
    }

    @Override
    public void setStartState(final Flow flow, final TransitionableState state) {
        setStartState(flow, state.getId());
    }

    @Override
    public EndState createEndState(final Flow flow, final String id) {
        return createEndState(flow, id, (ViewFactory) null);
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final String viewId, final boolean redirect) {
        if (!redirect) {
            return createEndState(flow, id, viewId);
        }
        return createEndState(flow, id, createExternalRedirectViewFactory(viewId));
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final String viewId) {
        return createEndState(flow, id, new LiteralExpression(viewId));
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final Expression expression) {
        val viewFactory = flowBuilderServices.getViewFactoryCreator()
            .createViewFactory(expression, flowBuilderServices.getExpressionParser(),
                flowBuilderServices.getConversionService(), null,
                flowBuilderServices.getValidator(), flowBuilderServices.getValidationHintResolver());
        return createEndState(flow, id, viewFactory);
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final ViewFactory viewFactory) {
        if (containsFlowState(flow, id)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return (EndState) flow.getStateInstance(id);
        }
        val endState = new EndState(flow, id);
        if (viewFactory != null) {
            val finalResponseAction = new ViewFactoryActionAdapter(viewFactory);
            endState.setFinalResponseAction(finalResponseAction);
            LOGGER.trace("Created end state state [{}] on flow id [{}], backed by view factory [{}]", id, flow.getId(), viewFactory);
        } else {
            LOGGER.trace("Created end state state [{}] on flow id [{}]", id, flow.getId());
        }
        return endState;
    }

    @Override
    public ViewState createViewState(final Flow flow, final String id, final Expression expression,
                                     final BinderConfiguration binder) {
        if (containsFlowState(flow, id)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return getTransitionableState(flow, id, ViewState.class);
        }
        val viewFactory = flowBuilderServices.getViewFactoryCreator()
            .createViewFactory(expression, flowBuilderServices.getExpressionParser(),
                flowBuilderServices.getConversionService(), binder, this.flowBuilderServices.getValidator(),
                flowBuilderServices.getValidationHintResolver());
        return createViewState(flow, id, viewFactory);
    }

    @Override
    public ViewState createViewState(final Flow flow, final String id, final ViewFactory viewFactory) {
        try {
            if (containsFlowState(flow, id)) {
                LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
                return getTransitionableState(flow, id, ViewState.class);
            }
            val viewState = new ViewState(flow, id, viewFactory);
            LOGGER.trace("Added view state [{}]", viewState.getId());
            return viewState;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public ViewState createViewState(final Flow flow, final String id, final String viewId) {
        return createViewState(flow, id, new LiteralExpression(viewId), null);
    }

    @Override
    public ViewState createViewState(final Flow flow, final String id, final String viewId, final BinderConfiguration binder) {
        return createViewState(flow, id, new LiteralExpression(viewId), binder);
    }

    @Override
    public ViewState createEndViewState(final Flow flow, final String id, final ViewFactory viewFactory) {
        if (containsFlowState(flow, id)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return getTransitionableState(flow, id, EndViewState.class);
        }
        return new EndViewState(flow, id, viewFactory);
    }

    @Override
    public ViewState createEndViewState(final Flow flow, final String id, final String viewId) {
        val viewFactory = flowBuilderServices.getViewFactoryCreator()
            .createViewFactory(new LiteralExpression(viewId), flowBuilderServices.getExpressionParser(),
                flowBuilderServices.getConversionService(), null, this.flowBuilderServices.getValidator(),
                flowBuilderServices.getValidationHintResolver());
        return createEndViewState(flow, id, viewFactory);
    }

    @Override
    public SubflowState createSubflowState(final Flow flow, final String stateId, final String subflow, final Action entryAction) {
        return createSubflowState(flow, stateId, subflow, this.flowDefinitionRegistry, entryAction);
    }

    @Override
    public SubflowState createSubflowState(final Flow flow,
                                           final String stateId,
                                           final String subflow,
                                           final FlowDefinitionRegistry registry,
                                           final Action entryAction) {
        if (containsFlowState(flow, stateId)) {
            LOGGER.trace("Flow [{}] already contains a definition for state id [{}]", flow.getId(), stateId);
            return getTransitionableState(flow, stateId, SubflowState.class);
        }
        val state = new SubflowState(flow, stateId, new BasicSubflowExpression(subflow, registry));
        if (entryAction != null) {
            state.getEntryActionList().add(entryAction);
        }
        return state;
    }

    @Override
    public Flow buildFlow(final String id) {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setParent(this.flowDefinitionRegistry);
        builder.addFlowBuilder(new FlowModelFlowBuilder(new DefaultFlowModelHolder(new DynamicFlowModelBuilder())), id);
        val registry = builder.build();
        return (Flow) registry.getFlowDefinition(id);
    }

    @Override
    public void createStateDefaultTransition(final TransitionableState state, final String targetState) {
        if (state == null) {
            LOGGER.trace("Cannot add default transition of [{}] to the given state is null and cannot be found in the flow.", targetState);
        } else {
            val transition = createTransition(targetState);
            state.getTransitionSet().add(transition);
        }
    }

    @Override
    public void createStateDefaultTransition(final TransitionableState state, final StateDefinition targetState) {
        createStateDefaultTransition(state, targetState.getId());
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final String targetState,
                                               final Map<String, Object> attributes) {
        return createTransitionForState(state, criteriaOutcome, targetState, false, attributes);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome) {
        return createTransitionForState(state, criteriaOutcome, StringUtils.EMPTY);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final String targetState) {
        return createTransitionForState(state, criteriaOutcome, targetState, Map.of());
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final String targetState,
                                               final Map<String, Object> attributes,
                                               final Action... actions) {
        return createTransitionForState(state, criteriaOutcome,
            targetState, false, attributes, actions);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final String targetState,
                                               final Action... actions) {
        return createTransitionForState(state, criteriaOutcome,
            targetState, false, Map.of(), actions);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final String targetState,
                                               final String... actions) {
        val finalActions = Arrays.stream(actions).map(this::createEvaluateAction).toArray(Action[]::new);
        return createTransitionForState(state, criteriaOutcome,
            targetState, false, Map.of(), finalActions);
    }

    @Override
    public Transition createTransitionForState(final Flow flow, final String stateId,
                                               final String criteriaOutcome,
                                               final String targetState) {
        return createTransitionForState(getTransitionableState(flow, stateId),
            criteriaOutcome, targetState, false, Map.of());
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome,
                                               final String targetState, final boolean removeExisting,
                                               final Map<String, Object> attributes, final Action... actions) {
        return FunctionUtils.doIfNotNull(state, () -> {
            try {
                if (removeExisting) {
                    var transition = (Transition) null;
                    do {
                        transition = (Transition) state.getTransition(criteriaOutcome);
                        if (transition != null) {
                            state.getTransitionSet().remove(transition);
                        }
                    } while (transition != null);
                }
                val transition = createTransition(criteriaOutcome, targetState, actions);
                attributes.forEach((key, value) -> transition.getAttributes().put(key, value));
                state.getTransitionSet().add(transition);
                LOGGER.trace("Added transition [{}] to the state [{}]", transition.getId(), state.getId());
                return transition;
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return null;
        });
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome,
                                               final String targetState, final boolean removeExisting,
                                               final Map<String, Object> attributes) {
        return createTransitionForState(state, criteriaOutcome, targetState,
            removeExisting, attributes, EMPTY_ACTIONS_ARRAY);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome,
                                               final String targetState, final boolean removeExisting) {
        return createTransitionForState(state, criteriaOutcome, targetState,
            removeExisting, Map.of(), EMPTY_ACTIONS_ARRAY);
    }

    @Override
    public Transition createTransitionForState(final TransitionableState state,
                                               final String criteriaOutcome,
                                               final TargetStateResolver targetStateResolver) {
        val transition = createTransition(criteriaOutcome, targetStateResolver);
        state.getTransitionSet().add(transition);
        return transition;
    }

    @Override
    public Transition insertTransitionForState(final TransitionableState state, final String criteriaOutcome,
                                               final String targetState) {
        val transition = createTransition(criteriaOutcome, targetState, EMPTY_ACTIONS_ARRAY);
        val field = ReflectionUtils.findField(state.getTransitionSet().getClass(), "transitions");
        ReflectionUtils.makeAccessible(field);
        val transitions = (List<Transition>) ReflectionUtils.getField(field, state.getTransitionSet());
        Objects.requireNonNull(transitions).addFirst(transition);
        LOGGER.trace("Added transition [{}] to the state [{}]", transition.getId(), state.getId());
        return transition;
    }

    @Override
    public Expression createExpression(final String expression, final Class expectedType) {
        val parserContext = new FluentParserContext().expectResult(expectedType);
        return getSpringExpressionParser().parseExpression(expression, parserContext);
    }

    @Override
    public Expression createExpression(final String expression) {
        return createExpression(expression, null);
    }

    @Override
    public boolean containsFlowState(final Flow flow, final String stateId) {
        if (flow == null) {
            LOGGER.error("Flow is not configured correctly and cannot be null.");
            return false;
        }
        return flow.containsState(stateId);
    }

    @Override
    public boolean containsSubflowState(final Flow flow, final String stateId) {
        return containsFlowState(flow, stateId) && getState(flow, stateId, SubflowState.class) != null;
    }

    @Override
    public boolean containsTransition(final TransitionableState state, final String transition) {
        if (state == null) {
            LOGGER.error("State is not configured correctly and cannot be null.");
            return false;
        }
        return state.getTransition(transition) != null;
    }

    @Override
    public FlowVariable createFlowVariable(final Flow flow, final String id, final Class type) {
        val opt = Arrays.stream(flow.getVariables()).filter(v -> v.getName().equalsIgnoreCase(id)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }
        val flowVar = new FlowVariable(id, new BeanFactoryVariableValueFactory(type, applicationContext.getAutowireCapableBeanFactory()));
        flow.addVariable(flowVar);
        return flowVar;
    }

    @Override
    public BinderConfiguration createStateBinderConfiguration(final Map<String, Map<String, String>> properties) {
        val binder = new BinderConfiguration();
        properties.forEach((key, value) -> {
            val converter = value.get("converter");
            val required = BooleanUtils.toBoolean(value.getOrDefault("required", Boolean.TRUE.toString()));
            val binding = new BinderConfiguration.Binding(key, converter, required);
            binder.addBinding(binding);
        });
        return binder;
    }

    @Override
    public BinderConfiguration createStateBinderConfiguration(final List<String> properties) {
        val binder = new BinderConfiguration();
        properties.forEach(p -> binder.addBinding(new BinderConfiguration.Binding(p, null, true)));
        return binder;
    }

    @Override
    public void createStateModelBinding(final TransitionableState state, final String modelName, final Class modelType) {
        LOGGER.trace("Creating model binding [{}] with type [{}] for [{}]", modelName, modelType, state.getId());
        state.getAttributes().put("model", createExpression(modelName, modelType));
    }

    @Override
    public BinderConfiguration getViewStateBinderConfiguration(final ViewState state) {
        val field = ReflectionUtils.findField(state.getViewFactory().getClass(), "binderConfiguration");
        ReflectionUtils.makeAccessible(field);
        return (BinderConfiguration) ReflectionUtils.getField(field, state.getViewFactory());
    }

    @Override
    public List<TransitionCriteria> getTransitionExecutionCriteriaChainForTransition(final Transition def) {
        if (def.getExecutionCriteria() instanceof final TransitionCriteriaChain chain) {
            val field = ReflectionUtils.findField(chain.getClass(), "criteriaChain");
            Assert.notNull(field, "criteriaChain cannot be null");
            ReflectionUtils.makeAccessible(field);
            return (List<TransitionCriteria>) ReflectionUtils.getField(field, chain);
        }
        if (def.getExecutionCriteria() != null) {
            return CollectionUtils.wrapList(def.getExecutionCriteria());
        }
        return new ArrayList<>();
    }

    @Override
    public <T> T getState(final Flow flow, final String stateId, final Class<T> clazz) {
        if (containsFlowState(flow, stateId)) {
            val state = flow.getState(stateId);
            return clazz.cast(state);
        }
        return null;
    }

    @Override
    public TransitionableState getState(final Flow flow, final String stateId) {
        return getState(flow, stateId, TransitionableState.class);
    }

    @Override
    public Flow getFlow(final String id) {
        return getFlow(this.flowDefinitionRegistry, id);
    }

    @Override
    public Flow getFlow(final FlowDefinitionRegistry registry, final String id) {
        if (registry == null) {
            LOGGER.warn("Flow registry is not configured and/or initialized correctly.");
            return null;
        }
        val found = List.of(registry.getFlowDefinitionIds()).contains(id);
        if (found) {
            return (Flow) registry.getFlowDefinition(id);
        }
        LOGGER.debug("[{}] could not find flow definition [{}]. Available flow definition ids are [{}]",
            getName(), id, registry.getFlowDefinitionIds());
        return null;
    }

    /**
     * Gets spring expression parser.
     *
     * @return the spring expression parser
     */
    public SpringELExpressionParser getSpringExpressionParser() {
        val configuration = new SpelParserConfiguration();
        val spelExpressionParser = new SpelExpressionParser(configuration);
        val parser = new SpringELExpressionParser(spelExpressionParser, this.flowBuilderServices.getConversionService());
        parser.addPropertyAccessor(new ActionPropertyAccessor());
        parser.addPropertyAccessor(new BeanExpressionContextAccessor());
        parser.addPropertyAccessor(new BeanFactoryPropertyAccessor());
        parser.addPropertyAccessor(new FlowVariablePropertyAccessor());
        parser.addPropertyAccessor(new MapAdaptablePropertyAccessor());
        parser.addPropertyAccessor(new MessageSourcePropertyAccessor());
        parser.addPropertyAccessor(new ScopeSearchingPropertyAccessor());
        parser.addPropertyAccessor(new MapAccessor());
        parser.addPropertyAccessor(new MapAdaptablePropertyAccessor());
        parser.addPropertyAccessor(new EnvironmentAccessor());
        parser.addPropertyAccessor(new ReflectivePropertyAccessor());
        return parser;
    }

    /**
     * Create mapper to subflow state.
     *
     * @param mappings the mappings
     * @param flow     the flow
     * @return the mapper
     */
    public Mapper createFlowInputMapper(final List<? extends Mapping> mappings, final Flow flow) {
        val flowInputMapper = flow.getInputMapper() == null ? new DefaultMapper() : (DefaultMapper) flow.getInputMapper();
        mappings
            .stream()
            .map(DefaultMapping.class::cast)
            .map(CasDefaultMapping::new)
            .forEach(flowInputMapper::addMapping);
        flow.setInputMapper(flowInputMapper);
        return flowInputMapper;
    }

    /**
     * Create flow input mapper.
     *
     * @param mappings the mappings
     * @return the mapper
     */
    public Mapper createFlowInputMapper(final List<? extends Mapping> mappings) {
        val flowInputMapper = new DefaultMapper();
        mappings
            .stream()
            .map(DefaultMapping.class::cast)
            .map(CasDefaultMapping::new)
            .forEach(flowInputMapper::addMapping);
        return flowInputMapper;
    }

    @Override
    public Mapping createFlowMapping(final String sourceExpression, final String targetExpression,
                                     final boolean required, final Class type) {
        val source = createExpression(sourceExpression);
        val target = createExpression(targetExpression);
        val mapping = new CasDefaultMapping(new DefaultMapping(source, target));
        mapping.setRequired(required);
        if (type != null) {
            val typeConverter = new RuntimeBindingConversionExecutor(type, flowBuilderServices.getConversionService());
            mapping.setTypeConverter(typeConverter);
        }
        return mapping;
    }

    /**
     * Create flow mapping.
     *
     * @param sourceExpression the source expression
     * @param targetExpression the target expression
     * @return the mapping
     */
    public Mapping createFlowMapping(final String sourceExpression, final String targetExpression) {
        return createFlowMapping(sourceExpression, targetExpression, false, null);
    }

    /**
     * Create subflow attribute mapper.
     *
     * @param inputMapper  the input mapper
     * @param outputMapper the output mapper
     * @return the subflow attribute mapper
     */
    public SubflowAttributeMapper createSubflowAttributeMapper(final Mapper inputMapper, final Mapper outputMapper) {
        return new GenericSubflowAttributeMapper(inputMapper, outputMapper);
    }

    /**
     * Clone action state.
     *
     * @param source the source
     * @param target the target
     */
    public void cloneActionState(final ActionState source, final ActionState target) {
        source.getActionList().forEach(a -> target.getActionList().add(a));
        source.getExitActionList().forEach(a -> target.getExitActionList().add(a));
        source.getAttributes().asMap().forEach((key, v) -> target.getAttributes().put(key, v));
        source.getTransitionSet().forEach(t -> target.getTransitionSet().addAll(t));
        val field = ReflectionUtils.findField(target.getExceptionHandlerSet().getClass(), "exceptionHandlers");
        Assert.notNull(field, "exceptionHandlers cannot be null");
        ReflectionUtils.makeAccessible(field);
        val list = (List<FlowExecutionExceptionHandler>) ReflectionUtils.getField(field, target.getExceptionHandlerSet());
        Objects.requireNonNull(list).forEach(h -> source.getExceptionHandlerSet().add(h));
        target.setDescription(source.getDescription());
        target.setCaption(source.getCaption());
    }

    /**
     * Gets expression string from action.
     *
     * @param act the act
     * @return the expression string from action
     */
    public Expression getExpressionStringFromAction(final EvaluateAction act) {
        val field = ReflectionUtils.findField(act.getClass(), "expression");
        Assert.notNull(field, "expression cannot be null");
        ReflectionUtils.makeAccessible(field);
        return (Expression) ReflectionUtils.getField(field, act);
    }

    /**
     * Create evaluate action for action state action.
     *
     * @param flow             the flow
     * @param actionStateId    the action state id
     * @param evaluateActionId the evaluate action id
     * @return the action
     */
    public Action createEvaluateActionForExistingActionState(final Flow flow, final String actionStateId, final String evaluateActionId) {
        val action = getState(flow, actionStateId, ActionState.class);
        val actions = action.getActionList().toArray();
        Arrays.stream(actions).forEach(action.getActionList()::remove);
        val evaluateAction = createEvaluateAction(evaluateActionId);
        action.getActionList().add(evaluateAction);
        action.getActionList().addAll(actions);
        return evaluateAction;
    }

    /**
     * Clone and create action state.
     *
     * @param flow                 the flow
     * @param actionStateId        the action state id
     * @param actionStateIdToClone the action state id to clone
     */
    public void createClonedActionState(final Flow flow, final String actionStateId, final String actionStateIdToClone) {
        val generateServiceTicket = getState(flow, actionStateIdToClone, ActionState.class);
        val consentTicketAction = createActionState(flow, actionStateId);
        cloneActionState(generateServiceTicket, consentTicketAction);
    }


    @Override
    public <T extends TransitionableState> T getTransitionableState(final Flow flow, final String stateId, final Class<T> clazz) {
        if (containsFlowState(flow, stateId)) {
            val state = flow.getTransitionableState(stateId);
            return clazz.cast(state);
        }
        return null;
    }

    /**
     * Gets transitionable state.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return the transitionable state
     */
    public TransitionableState getTransitionableState(final Flow flow, final String stateId) {
        if (containsFlowState(flow, stateId)) {
            return flow.getTransitionableState(stateId);
        }
        return null;
    }

    /**
     * Create transitions for state.
     *
     * @param flow               the flow
     * @param stateId            the state id
     * @param criteriaAndTargets the criteria and targets
     */
    public void createTransitionsForState(final Flow flow, final String stateId, final Map<String, String> criteriaAndTargets) {
        if (containsFlowState(flow, stateId)) {
            val state = getState(flow, stateId);
            criteriaAndTargets.forEach((key, v) -> createTransitionForState(state, key, v));
        }
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow    the flow
     * @param stateId the action state id
     * @param actions the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final String stateId, final Object... actions) {
        val evalActions = Arrays.stream(actions)
            .map(givenAction -> {
                if (givenAction instanceof final Action action) {
                    return action;
                }
                return createEvaluateAction(givenAction.toString());
            })
            .toArray(Action[]::new);
        addActionsToActionStateExecutionListAt(flow, stateId, 0, evalActions);
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final TransitionableState actionStateId, final Action... actions) {
        addActionsToActionStateExecutionListAt(flow, actionStateId.getId(), 0, actions);
    }

    /**
     * Add actions to action state execution list at.
     *
     * @param flow     the flow
     * @param stateId  the action state id
     * @param position the position
     * @param actions  the actions
     */
    public void addActionsToActionStateExecutionListAt(final Flow flow, final String stateId, final int position,
                                                       final Action... actions) {
        if (flow != null) {
            val givenState = getState(flow, stateId, TransitionableState.class);
            var actionList = new ActionList();
            if (givenState instanceof final ActionState as) {
                actionList = as.getActionList();
            }
            if (givenState instanceof final ViewState vs) {
                actionList = vs.getEntryActionList();
            }
            val currentActions = new ArrayList<Action>(actionList.size() + actions.length);
            actionList.forEach(currentActions::add);
            val index = position < 0 || position == Integer.MAX_VALUE ? currentActions.size() : position;
            currentActions.forEach(actionList::remove);
            currentActions.addAll(index, Arrays.stream(actions).toList());
            actionList.addAll(currentActions.toArray(Action[]::new));
            LOGGER.trace("Final (entry) action list for state [{}] is [{}]", stateId, actionList);
        }
    }


    @Override
    public ViewFactory createExternalRedirectViewFactory(final String expressionId) {
        val expression = createExpression(expressionId, String.class);
        return new ActionExecutingViewFactory(new ExternalRedirectAction(expression));
    }

    protected void doInitialize() {
    }

    private static class CasDefaultMapping extends DefaultMapping {
        CasDefaultMapping(final DefaultMapping mapping) {
            super(mapping.getSourceExpression(), mapping.getTargetExpression());
            setRequired(mapping.isRequired());
            setTypeConverter(mapping.getTypeConverter());
        }

        @Override
        public void map(final DefaultMappingContext context) {
            if (getSourceExpression().getValue(context.getSource()) != null) {
                super.map(context);
            }
        }
    }
}
