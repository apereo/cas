package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.service.RuntimeBindingConversionExecutor;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserContext;
import org.springframework.binding.expression.spel.SpringELExpressionParser;
import org.springframework.binding.expression.support.FluentParserContext;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.ExternalRedirectAction;
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
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.WildcardTransitionCriteria;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
@RequiredArgsConstructor
@ToString(of = "name")
public abstract class AbstractCasWebflowConfigurer implements CasWebflowConfigurer {
    /**
     * The logout flow definition registry.
     */
    protected FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    /**
     * Flow builder services.
     */
    protected final FlowBuilderServices flowBuilderServices;

    /**
     * The Login flow definition registry.
     */
    protected final FlowDefinitionRegistry loginFlowDefinitionRegistry;

    /**
     * Application context.
     */
    protected final ApplicationContext applicationContext;

    /**
     * CAS Properties.
     */
    protected final CasConfigurationProperties casProperties;

    private int order;

    private String name = getClass().getSimpleName();

    @Override
    public void initialize() {
        try {
            LOGGER.debug("Initializing CAS webflow configuration...");
            if (casProperties.getWebflow().isAutoconfigure()) {
                doInitialize();
            } else {
                LOGGER.warn("Webflow auto-configuration is disabled. CAS will not modify the webflow via [{}]", getClass().getName());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Handle the initialization of the webflow.
     */
    protected abstract void doInitialize();

    @Override
    public Flow buildFlow(final String location, final String id) {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setParent(this.loginFlowDefinitionRegistry);
        builder.addFlowLocation(location, id);
        final FlowDefinitionRegistry registry = builder.build();
        return (Flow) registry.getFlowDefinition(id);
    }

    @Override
    public Flow getLoginFlow() {
        if (this.loginFlowDefinitionRegistry == null) {
            LOGGER.error("Login flow registry is not configured and/or initialized correctly.");
            return null;
        }
        final boolean found = Arrays.stream(this.loginFlowDefinitionRegistry.getFlowDefinitionIds()).anyMatch(f -> f.equals(FLOW_ID_LOGIN));
        if (found) {
            return (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(FLOW_ID_LOGIN);
        }
        LOGGER.error("Could not find flow definition [{}]. Available flow definition ids are [{}]", FLOW_ID_LOGIN, this.loginFlowDefinitionRegistry.getFlowDefinitionIds());
        return null;
    }

    @Override
    public Flow getLogoutFlow() {
        if (this.logoutFlowDefinitionRegistry == null) {
            LOGGER.warn("Logout flow registry is not configured correctly.");
            return null;
        }
        return (Flow) this.logoutFlowDefinitionRegistry.getFlowDefinition(FLOW_ID_LOGOUT);
    }

    @Override
    public TransitionableState getStartState(final Flow flow) {
        return TransitionableState.class.cast(flow.getStartState());
    }

    /**
     * Create action state action state.
     *
     * @param flow the flow
     * @param name the name
     * @return the action state
     */
    public ActionState createActionState(final Flow flow, final String name) {
        return createActionState(flow, name, new Action[]{});
    }

    /**
     * Create action state action state.
     *
     * @param flow   the flow
     * @param name   the name
     * @param action the action
     * @return the action state
     */
    public ActionState createActionState(final Flow flow, final String name, final String action) {
        return createActionState(flow, name, createEvaluateAction(action));
    }

    /**
     * Create action state action state.
     *
     * @param flow   the flow
     * @param name   the name
     * @param action the action
     * @return the action state
     */
    public ActionState createActionState(final Flow flow, final String name, final Action action) {
        return createActionState(flow, name, new Action[]{action});
    }

    @Override
    public ActionState createActionState(final Flow flow, final String name, final Action... actions) {
        if (containsFlowState(flow, name)) {
            LOGGER.debug("Flow [{}] already contains a definition for state id [{}]", flow.getId(), name);
            return getTransitionableState(flow, name, ActionState.class);
        }
        final ActionState actionState = new ActionState(flow, name);
        LOGGER.debug("Created action state [{}]", actionState.getId());
        actionState.getActionList().addAll(actions);
        LOGGER.debug("Added action to the action state [{}] list of actions: [{}]", actionState.getId(), actionState.getActionList());
        return actionState;
    }

    @Override
    public DecisionState createDecisionState(final Flow flow, final String id, final String testExpression, final String thenStateId, final String elseStateId) {
        if (containsFlowState(flow, id)) {
            LOGGER.debug("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return getTransitionableState(flow, id, DecisionState.class);
        }
        final DecisionState decisionState = new DecisionState(flow, id);
        final Expression expression = createExpression(testExpression, Boolean.class);
        final Transition thenTransition = createTransition(expression, thenStateId);
        decisionState.getTransitionSet().add(thenTransition);
        final Transition elseTransition = createTransition("*", elseStateId);
        decisionState.getTransitionSet().add(elseTransition);
        return decisionState;
    }

    @Override
    public void setStartState(final Flow flow, final String state) {
        flow.setStartState(state);
        final TransitionableState startState = getStartState(flow);
        LOGGER.debug("Start state is now set to [{}]", startState.getId());
    }

    @Override
    public void setStartState(final Flow flow, final TransitionableState state) {
        setStartState(flow, state.getId());
    }

    @Override
    public EvaluateAction createEvaluateAction(final String expression) {
        if (this.flowBuilderServices == null) {
            LOGGER.error("Flow builder services is not configured correctly.");
            return null;
        }
        final ParserContext ctx = new FluentParserContext();
        final Expression action = this.flowBuilderServices.getExpressionParser().parseExpression(expression, ctx);
        final EvaluateAction newAction = new EvaluateAction(action, null);
        LOGGER.debug("Created evaluate action for expression [{}]", action.getExpressionString());
        return newAction;
    }

    /**
     * Add a default transition to a given state.
     *
     * @param state       the state to include the default transition
     * @param targetState the id of the destination state to which the flow should transfer
     */
    public void createStateDefaultTransition(final TransitionableState state, final String targetState) {
        if (state == null) {
            LOGGER.debug("Cannot add default transition of [{}] to the given state is null and cannot be found in the flow.", targetState);
            return;
        }
        final Transition transition = createTransition(targetState);
        state.getTransitionSet().add(transition);
    }


    /**
     * Create state default transition.
     *
     * @param state       the state
     * @param targetState the target state
     */
    public void createStateDefaultTransition(final TransitionableState state, final StateDefinition targetState) {
        createStateDefaultTransition(state, targetState.getId());
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final ActionState actionStateId, final String... actions) {
        prependActionsToActionStateExecutionList(flow, actionStateId.getId(), actions);
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final String actionStateId, final String... actions) {
        final EvaluateAction[] evalActions = Arrays.stream(actions)
            .map(this::createEvaluateAction)
            .toArray(EvaluateAction[]::new);
        addActionsToActionStateExecutionListAt(flow, actionStateId, 0, evalActions);
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final String actionStateId, final EvaluateAction... actions) {
        addActionsToActionStateExecutionListAt(flow, actionStateId, 0, actions);
    }

    /**
     * Prepend actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void prependActionsToActionStateExecutionList(final Flow flow, final ActionState actionStateId, final EvaluateAction... actions) {
        addActionsToActionStateExecutionListAt(flow, actionStateId.getId(), 0, actions);
    }

    /**
     * Append actions to action state execution list.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param actions       the actions
     */
    public void appendActionsToActionStateExecutionList(final Flow flow, final String actionStateId, final EvaluateAction... actions) {
        addActionsToActionStateExecutionListAt(flow, actionStateId, Integer.MAX_VALUE, actions);
    }

    /**
     * Add actions to action state execution list at.
     *
     * @param flow          the flow
     * @param actionStateId the action state id
     * @param position      the position
     * @param actions       the actions
     */
    public void addActionsToActionStateExecutionListAt(final Flow flow, final String actionStateId, final int position, final EvaluateAction... actions) {
        final ActionState actionState = getState(flow, actionStateId, ActionState.class);
        final List<Action> currentActions = new ArrayList<Action>();
        final ActionList actionList = actionState.getActionList();
        actionList.forEach(currentActions::add);
        final int index = position < 0 || position == Integer.MAX_VALUE ? currentActions.size() : position;
        currentActions.forEach(actionList::remove);
        Arrays.stream(actions).forEach(a -> currentActions.add(index, a));
        actionList.addAll(currentActions.toArray(new Action[]{}));
    }


    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome, final String targetState) {
        return createTransitionForState(state, criteriaOutcome, targetState, false);
    }

    /**
     * Add transition to action state.
     *
     * @param state           the action state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param removeExisting  the remove existing
     * @return the transition
     */
    public Transition createTransitionForState(final TransitionableState state, final String criteriaOutcome, final String targetState, final boolean removeExisting) {
        try {
            if (removeExisting) {
                final Transition success = (Transition) state.getTransition(criteriaOutcome);
                if (success != null) {
                    state.getTransitionSet().remove(success);
                }
            }
            final Transition transition = createTransition(criteriaOutcome, targetState);
            state.getTransitionSet().add(transition);
            LOGGER.debug("Added transition [{}] to the state [{}]", transition.getId(), state.getId());
            return transition;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Transition createTransition(final String criteriaOutcome, final String targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState);
    }

    @Override
    public Transition createTransition(final String criteriaOutcome, final TransitionableState targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState.getId());
    }

    @Override
    public Transition createTransition(final String targetState) {
        final DefaultTargetStateResolver resolver = new DefaultTargetStateResolver(targetState);
        return new Transition(resolver);
    }

    @Override
    public Transition createTransition(final Expression criteriaOutcomeExpression, final String targetState) {
        final TransitionCriteria criteria;
        if (criteriaOutcomeExpression.toString().equals(WildcardTransitionCriteria.WILDCARD_EVENT_ID)) {
            criteria = WildcardTransitionCriteria.INSTANCE;
        } else {
            criteria = new DefaultTransitionCriteria(criteriaOutcomeExpression);
        }
        final DefaultTargetStateResolver resolver = new DefaultTargetStateResolver(targetState);
        final Transition t = new Transition(criteria, resolver);
        return t;
    }

    /**
     * Create expression expression.
     *
     * @param expression   the expression
     * @param expectedType the expected type
     * @return the expression
     */
    public Expression createExpression(final String expression, final Class expectedType) {
        final ParserContext parserContext = new FluentParserContext().expectResult(expectedType);
        return getSpringExpressionParser().parseExpression(expression, parserContext);
    }

    /**
     * Create expression.
     *
     * @param expression the expression
     * @return the expression
     */
    public Expression createExpression(final String expression) {
        return createExpression(expression, null);
    }

    /**
     * Gets spring expression parser.
     *
     * @return the spring expression parser
     */
    public SpringELExpressionParser getSpringExpressionParser() {
        final SpelParserConfiguration configuration = new SpelParserConfiguration();
        final SpelExpressionParser spelExpressionParser = new SpelExpressionParser(configuration);
        final SpringELExpressionParser parser = new SpringELExpressionParser(spelExpressionParser, this.flowBuilderServices.getConversionService());
        parser.addPropertyAccessor(new ActionPropertyAccessor());
        parser.addPropertyAccessor(new BeanFactoryPropertyAccessor());
        parser.addPropertyAccessor(new FlowVariablePropertyAccessor());
        parser.addPropertyAccessor(new MapAdaptablePropertyAccessor());
        parser.addPropertyAccessor(new MessageSourcePropertyAccessor());
        parser.addPropertyAccessor(new ScopeSearchingPropertyAccessor());
        parser.addPropertyAccessor(new BeanExpressionContextAccessor());
        parser.addPropertyAccessor(new MapAccessor());
        parser.addPropertyAccessor(new MapAdaptablePropertyAccessor());
        parser.addPropertyAccessor(new EnvironmentAccessor());
        parser.addPropertyAccessor(new ReflectivePropertyAccessor());
        return parser;
    }

    @Override
    public EndState createEndState(final Flow flow, final String id) {
        return createEndState(flow, id, (ViewFactory) null);
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final String viewId) {
        return createEndState(flow, id, new LiteralExpression(viewId));
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final Expression expression) {
        final ViewFactory viewFactory = this.flowBuilderServices.getViewFactoryCreator()
            .createViewFactory(expression, this.flowBuilderServices.getExpressionParser(),
                this.flowBuilderServices.getConversionService(), null,
                this.flowBuilderServices.getValidator(), this.flowBuilderServices.getValidationHintResolver());
        return createEndState(flow, id, viewFactory);
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final String viewId, final boolean redirect) {
        if (!redirect) {
            return createEndState(flow, id, viewId);
        }
        final Expression expression = createExpression(viewId, String.class);
        final ActionExecutingViewFactory viewFactory = new ActionExecutingViewFactory(new ExternalRedirectAction(expression));
        return createEndState(flow, id, viewFactory);
    }

    @Override
    public EndState createEndState(final Flow flow, final String id, final ViewFactory viewFactory) {
        if (containsFlowState(flow, id)) {
            LOGGER.debug("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return (EndState) flow.getStateInstance(id);
        }
        final EndState endState = new EndState(flow, id);
        if (viewFactory != null) {
            final Action finalResponseAction = new ViewFactoryActionAdapter(viewFactory);
            endState.setFinalResponseAction(finalResponseAction);
            LOGGER.debug("Created end state state [{}] on flow id [{}], backed by view factory [{}]", id, flow.getId(), viewFactory);
        } else {
            LOGGER.debug("Created end state state [{}] on flow id [{}]", id, flow.getId());
        }
        return endState;
    }

    @Override
    public ViewState createViewState(final Flow flow, final String id, final Expression expression,
                                     final BinderConfiguration binder) {
        try {
            if (containsFlowState(flow, id)) {
                LOGGER.debug("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
                return getTransitionableState(flow, id, ViewState.class);
            }
            final ViewFactory viewFactory = this.flowBuilderServices.getViewFactoryCreator()
                .createViewFactory(expression, this.flowBuilderServices.getExpressionParser(),
                    this.flowBuilderServices.getConversionService(), binder, this.flowBuilderServices.getValidator(),
                    this.flowBuilderServices.getValidationHintResolver());
            final ViewState viewState = new ViewState(flow, id, viewFactory);
            LOGGER.debug("Added view state [{}]", viewState.getId());
            return viewState;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
    public SubflowState createSubflowState(final Flow flow, final String id, final String subflow, final Action entryAction) {
        if (containsFlowState(flow, id)) {
            LOGGER.debug("Flow [{}] already contains a definition for state id [{}]", flow.getId(), id);
            return getTransitionableState(flow, id, SubflowState.class);
        }
        final SubflowState state = new SubflowState(flow, id, new BasicSubflowExpression(subflow, this.loginFlowDefinitionRegistry));
        if (entryAction != null) {
            state.getEntryActionList().add(entryAction);
        }
        return state;
    }

    @Override
    public SubflowState createSubflowState(final Flow flow, final String id, final String subflow) {
        return createSubflowState(flow, id, subflow, null);
    }

    /**
     * Create mapper to subflow state.
     *
     * @param mappings the mappings
     * @return the mapper
     */
    public Mapper createMapperToSubflowState(final List<DefaultMapping> mappings) {
        final DefaultMapper inputMapper = new DefaultMapper();
        mappings.forEach(inputMapper::addMapping);
        return inputMapper;
    }

    /**
     * Create mapping to subflow state.
     *
     * @param name     the name
     * @param value    the value
     * @param required the required
     * @param type     the type
     * @return the default mapping
     */
    public DefaultMapping createMappingToSubflowState(final String name, final String value, final boolean required, final Class type) {
        final ExpressionParser parser = this.flowBuilderServices.getExpressionParser();
        final Expression source = parser.parseExpression(value, new FluentParserContext());
        final Expression target = parser.parseExpression(name, new FluentParserContext());
        final DefaultMapping mapping = new DefaultMapping(source, target);
        mapping.setRequired(required);
        final ConversionExecutor typeConverter = new RuntimeBindingConversionExecutor(type, this.flowBuilderServices.getConversionService());
        mapping.setTypeConverter(typeConverter);
        return mapping;
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
     * Contains flow state?
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return true if flow contains the state.
     */
    public boolean containsFlowState(final Flow flow, final String stateId) {
        if (flow == null) {
            LOGGER.error("Flow is not configured correctly and cannot be null.");
            return false;
        }
        return flow.containsState(stateId);
    }

    /**
     * Contains subflow state.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return the boolean
     */
    public boolean containsSubflowState(final Flow flow, final String stateId) {
        if (containsFlowState(flow, stateId)) {
            return getState(flow, stateId, SubflowState.class) != null;
        }
        return false;
    }

    /**
     * Contains transition boolean.
     *
     * @param state      the state
     * @param transition the transition
     * @return the boolean
     */
    public boolean containsTransition(final TransitionableState state, final String transition) {
        if (state == null) {
            LOGGER.error("State is not configured correctly and cannot be null.");
            return false;
        }
        return state.getTransition(transition) != null;
    }

    /**
     * Create flow variable flow variable.
     *
     * @param flow the flow
     * @param id   the id
     * @param type the type
     * @return the flow variable
     */
    public FlowVariable createFlowVariable(final Flow flow, final String id, final Class type) {
        final Optional<FlowVariable> opt = Arrays.stream(flow.getVariables()).filter(v -> v.getName().equalsIgnoreCase(id)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }
        final FlowVariable flowVar = new FlowVariable(id, new BeanFactoryVariableValueFactory(type, applicationContext.getAutowireCapableBeanFactory()));
        flow.addVariable(flowVar);
        return flowVar;
    }

    /**
     * Create state model bindings.
     *
     * @param properties the properties
     * @return the binder configuration
     */
    public BinderConfiguration createStateBinderConfiguration(final List<String> properties) {
        final BinderConfiguration binder = new BinderConfiguration();
        properties.forEach(p -> binder.addBinding(new BinderConfiguration.Binding(p, null, true)));
        return binder;
    }

    /**
     * Create state model binding.
     *
     * @param state     the state
     * @param modelName the model name
     * @param modelType the model type
     */
    public void createStateModelBinding(final TransitionableState state, final String modelName, final Class modelType) {
        state.getAttributes().put("model", createExpression(modelName, modelType));
    }

    /**
     * Gets state binder configuration.
     *
     * @param state the state
     * @return the state binder configuration
     */
    public BinderConfiguration getViewStateBinderConfiguration(final ViewState state) {
        final Field field = ReflectionUtils.findField(state.getViewFactory().getClass(), "binderConfiguration");
        ReflectionUtils.makeAccessible(field);
        return (BinderConfiguration) ReflectionUtils.getField(field, state.getViewFactory());
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
        source.getAttributes().asMap().forEach((k, v) -> target.getAttributes().put(k, v));
        source.getTransitionSet().forEach(t -> target.getTransitionSet().addAll(t));
        final Field field = ReflectionUtils.findField(target.getExceptionHandlerSet().getClass(), "exceptionHandlers");
        ReflectionUtils.makeAccessible(field);
        final List<FlowExecutionExceptionHandler> list = (List<FlowExecutionExceptionHandler>) ReflectionUtils.getField(field, target.getExceptionHandlerSet());
        list.forEach(h -> source.getExceptionHandlerSet().add(h));
        target.setDescription(source.getDescription());
        target.setCaption(source.getCaption());
    }

    /**
     * Gets transition execution criteria chain for transition.
     *
     * @param def the def
     * @return the transition execution criteria chain for transition
     */
    public List<TransitionCriteria> getTransitionExecutionCriteriaChainForTransition(final Transition def) {
        if (def.getExecutionCriteria() instanceof TransitionCriteriaChain) {
            final TransitionCriteriaChain chain = (TransitionCriteriaChain) def.getExecutionCriteria();
            final Field field = ReflectionUtils.findField(chain.getClass(), "criteriaChain");
            ReflectionUtils.makeAccessible(field);
            return (List<TransitionCriteria>) ReflectionUtils.getField(field, chain);
        }
        if (def.getExecutionCriteria() != null) {
            final List c = new ArrayList<>();
            c.add(def.getExecutionCriteria());
            return c;
        }
        return new ArrayList<>(0);
    }

    /**
     * Gets expression string from action.
     *
     * @param act the act
     * @return the expression string from action
     */
    public Expression getExpressionStringFromAction(final EvaluateAction act) {
        final Field field = ReflectionUtils.findField(act.getClass(), "expression");
        ReflectionUtils.makeAccessible(field);
        return (Expression) ReflectionUtils.getField(field, act);
    }

    /**
     * Register multifactor providers state transitions into webflow.
     *
     * @param state the state
     */
    public void registerMultifactorProvidersStateTransitionsIntoWebflow(final TransitionableState state) {
        final Map<String, MultifactorAuthenticationProvider> providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        providerMap.forEach((k, v) -> createTransitionForState(state, v.getId(), v.getId()));
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
        final ActionState action = getState(flow, actionStateId, ActionState.class);
        final Action[] actions = action.getActionList().toArray();
        Arrays.stream(actions).forEach(action.getActionList()::remove);
        final Action evaluateAction = createEvaluateAction(evaluateActionId);
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
        final ActionState generateServiceTicket = getState(flow, actionStateIdToClone, ActionState.class);
        final ActionState consentTicketAction = createActionState(flow, actionStateId);
        cloneActionState(generateServiceTicket, consentTicketAction);
    }

    /**
     * Gets state.
     *
     * @param <T>     the type parameter
     * @param flow    the flow
     * @param stateId the state id
     * @param clazz   the clazz
     * @return the state
     */
    public <T> T getState(final Flow flow, final String stateId, final Class<T> clazz) {
        if (containsFlowState(flow, stateId)) {
            final StateDefinition state = flow.getState(stateId);
            return clazz.cast(state);
        }
        return null;
    }

    /**
     * Gets state.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return the state
     */
    public TransitionableState getState(final Flow flow, final String stateId) {
        return getState(flow, stateId, TransitionableState.class);
    }

    /**
     * Gets transitionable state.
     *
     * @param <T>     the type parameter
     * @param flow    the flow
     * @param stateId the state id
     * @param clazz   the clazz
     * @return the transitionable state
     */
    public <T extends TransitionableState> T getTransitionableState(final Flow flow, final String stateId, final Class<T> clazz) {
        if (containsFlowState(flow, stateId)) {
            final StateDefinition state = flow.getTransitionableState(stateId);
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
            return TransitionableState.class.cast(flow.getTransitionableState(stateId));
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
            final TransitionableState state = getState(flow, stateId);
            criteriaAndTargets.forEach((k, v) -> createTransitionForState(state, k, v));
        }
    }

}
