package org.jasig.cas.web.flow;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.ViewFactoryActionAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowAttributeMapper;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.WildcardTransitionCriteria;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.engine.support.GenericSubflowAttributeMapper;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.expression.spel.ActionPropertyAccessor;
import org.springframework.webflow.expression.spel.BeanFactoryPropertyAccessor;
import org.springframework.webflow.expression.spel.FlowVariablePropertyAccessor;
import org.springframework.webflow.expression.spel.MapAdaptablePropertyAccessor;
import org.springframework.webflow.expression.spel.MessageSourcePropertyAccessor;
import org.springframework.webflow.expression.spel.ScopeSearchingPropertyAccessor;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * The {@link AbstractCasWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("casWebflowConfigurer")
public abstract class AbstractCasWebflowConfigurer {
    /**
     * The transition state 'success'.
     */
    protected static final String TRANSITION_ID_SUCCESS = "success";

    /**
     * The transition state 'yes'.
     */
    protected static final String TRANSITION_ID_YES = "yes";

    /**
     * The transition state 'no'.
     */
    protected static final String TRANSITION_ID_NO = "no";

    /**
     * The transition state 'submit'.
     */
    protected static final String TRANSITION_ID_SUBMIT = "submit";
    /**
     * The transition state 'generated'.
     */
    protected static final String TRANSITION_ID_GENERATED = "generated";
    /**
     * The transition state 'error'.
     */
    protected static final String TRANSITION_ID_ERROR = "error";
    /**
     * The transition state 'warn'.
     */
    protected static final String TRANSITION_ID_WARN = "warn";

    /**
     * The transition state 'sendTicketGrantingTicket'.
     */
    protected static final String TRANSITION_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicket";

    /**
     * The transition state 'viewLoginForm'.
     */
    protected static final String TRANSITION_ID_VIEW_LOGIN_FORM = "viewLoginForm";


    /**
     * The transition state 'ticketGrantingTicketCheck'.
     */
    protected static final String TRANSITION_ID_TICKET_GRANTING_TICKET_CHECK = "ticketGrantingTicketCheck";

    /**
     * The action state 'generateLoginTicket'.
     */
    protected static final String STATE_ID_GENERATE_LOGIN_TICKET = "generateLoginTicket";

    private static final String FLOW_ID_LOGIN = "login";

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * Initialize.
     */
    @PostConstruct
    public final void initialize() {
        try {
            logger.debug("Initializing CAS webflow configuration...");
            doInitialize();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Handle the initialization of the webflow.
     *
     * @throws Exception the exception
     */
    protected abstract void doInitialize() throws Exception;

    /**
     * Gets login flow.
     *
     * @return the login flow
     */
    protected Flow getLoginFlow() {
        final Flow flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(FLOW_ID_LOGIN);
        return flow;
    }

    protected List<String> getFlowDefinitionIds() {
        return Arrays.asList(loginFlowDefinitionRegistry.getFlowDefinitionIds());
    }

    /**
     * Gets flow definition ids.
     *
     * @param excludedFlowIds the excluded flow ids
     * @return the flow definition ids
     */
    protected List<String> getFlowDefinitionIds(final String[] excludedFlowIds) {
        String[] flowIds = loginFlowDefinitionRegistry.getFlowDefinitionIds();

        for (final String flowId : excludedFlowIds) {
            flowIds = ArrayUtils.removeElement(flowIds, flowId);
        }
        return Arrays.asList(flowIds);
    }

    /**
     * Gets start state.
     *
     * @param flow the flow
     * @return the start state
     */
    protected TransitionableState getStartState(final Flow flow) {
        final TransitionableState currentStartState = TransitionableState.class.cast(flow.getStartState());
        return currentStartState;
    }

    /**
     * Create action state.
     *
     * @param flow    the flow
     * @param name    the name
     * @param actions the actions
     * @return the action state
     */
    protected ActionState createActionState(final Flow flow, final String name, final Action... actions) {
        final ActionState actionState = new ActionState(flow, name);
        logger.debug("Created action state {}", actionState.getId());
        actionState.getActionList().addAll(actions);
        logger.debug("Added action to the action state {} list of actions: {}", actionState.getId(), actionState.getActionList());
        return actionState;
    }

    /**
     * Create decision state decision state.
     *
     * @param flow           the flow
     * @param id             the id
     * @param testExpression the test expression
     * @param thenStateId    the then state id
     * @param elseStateId    the else state id
     * @return the decision state
     */
    protected DecisionState createDecisionState(final Flow flow, final String id, final String testExpression,
                                                final String thenStateId, final String elseStateId) {
        final DecisionState decisionState = new DecisionState(flow, id);

        final Expression expression = createExpression(flow, testExpression, Boolean.class);
        final Transition thenTransition = createTransition(expression, thenStateId);
        decisionState.getTransitionSet().add(thenTransition);

        final Transition elseTransition = createTransition("*", elseStateId);
        decisionState.getTransitionSet().add(elseTransition);

        return decisionState;

    }

    /**
     * Sets start state.
     *
     * @param flow  the flow
     * @param state the state
     */
    protected void setStartState(final Flow flow, final String state) {
        flow.setStartState(state);
        final TransitionableState startState = getStartState(flow);
        logger.debug("Start state is now set to {}", startState.getId());
    }

    /**
     * Sets start state.
     *
     * @param flow  the flow
     * @param state the state
     */
    protected void setStartState(final Flow flow, final TransitionableState state) {
        setStartState(flow, state.getId());
    }

    /**
     * Add global transition if exception is thrown.
     *
     * @param flow          the flow
     * @param targetStateId the target state id
     * @param clazz         the exception class
     */
    protected void createGlobalTransition(final Flow flow, final String targetStateId,
                                          final Class<? extends Throwable> clazz) {

        try {
            final TransitionExecutingFlowExecutionExceptionHandler handler = new TransitionExecutingFlowExecutionExceptionHandler();
            final TargetStateResolver targetStateResolver = (TargetStateResolver) fromStringTo(TargetStateResolver.class)
                    .execute(targetStateId);
            handler.add(clazz, targetStateResolver);

            logger.debug("Added transition {} to execute on the occurrence of {}", targetStateId, clazz.getName());
            flow.getExceptionHandlerSet().add(handler);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }


    }

    /**
     * From string to class type, based on the flow conversion service.
     *
     * @param targetType the target type
     * @return the conversion executor
     */
    protected ConversionExecutor fromStringTo(final Class targetType) {
        return this.flowBuilderServices.getConversionService().getConversionExecutor(String.class, targetType);
    }

    /**
     * Loads the specified class by name, either based on the conversion service
     * or by the flow classloader.
     *
     * @param name the name
     * @return the class
     */
    private Class toClass(final String name) {
        final Class clazz = this.flowBuilderServices.getConversionService().getClassForAlias(name);
        if (clazz != null) {
            return clazz;
        }

        try {
            final ClassLoader classLoader = this.flowBuilderServices.getApplicationContext().getClassLoader();
            return ClassUtils.forName(name, classLoader);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class " + name);
        }

    }

    /**
     * Create evaluate action.
     *
     * @param expression the expression
     * @return the evaluate action
     */
    protected EvaluateAction createEvaluateAction(final String expression) {
        final ParserContext ctx = new FluentParserContext();
        final Expression action = this.flowBuilderServices.getExpressionParser()
                .parseExpression(expression, ctx);
        final EvaluateAction newAction = new EvaluateAction(action, null);

        logger.debug("Created evaluate action for expression {}", action.getExpressionString());
        return newAction;
    }

    /**
     * Add a default transition to a given state.
     *
     * @param state       the state to include the default transition
     * @param targetState the id of the destination state to which the flow should transfer
     */
    protected void createStateDefaultTransition(final TransitionableState state, final String targetState) {
        if (state == null) {
            logger.debug("Cannot add default transition of [{}] to the given state is null and cannot be found in the flow.", targetState);
            return;
        }
        final Transition transition = createTransition(targetState);
        state.getTransitionSet().add(transition);
    }

    /**
     * Add transition to action state.
     *
     * @param state           the action state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     */
    protected void createTransitionForState(final TransitionableState state,
                                            final String criteriaOutcome, final String targetState) {
        try {
            final Transition transition = createTransition(criteriaOutcome, targetState);
            state.getTransitionSet().add(transition);
            logger.debug("Added transition {} to the state {}", transition.getId(), state.getId());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Create transition.
     *
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    protected Transition createTransition(final String criteriaOutcome, final String targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState);
    }

    /**
     * Create transition.
     *
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    protected Transition createTransition(final String criteriaOutcome, final TransitionableState targetState) {
        return createTransition(new LiteralExpression(criteriaOutcome), targetState.getId());
    }

    /**
     * Create transition.
     *
     * @param targetState               the target state
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @return the transition
     */
    protected Transition createTransition(final Expression criteriaOutcomeExpression, final String targetState) {
        final TransitionCriteria criteria;

        if (criteriaOutcomeExpression.toString().equals(WildcardTransitionCriteria.WILDCARD_EVENT_ID)) {
            criteria = WildcardTransitionCriteria.INSTANCE;
        } else {
            criteria = new DefaultTransitionCriteria(criteriaOutcomeExpression);
        }

        final DefaultTargetStateResolver resolver = new DefaultTargetStateResolver(targetState);
        return new Transition(criteria, resolver);
    }

    /**
     * Create expression expression.
     *
     * @param flow         the flow
     * @param expression   the expression
     * @param expectedType the expected type
     * @return the expression
     */
    protected Expression createExpression(final Flow flow, final String expression, final Class expectedType) {
        final ParserContext parserContext = new FluentParserContext()
                .expectResult(expectedType);
        return getSpringExpressionParser().parseExpression(expression, parserContext);
    }

    /**
     * Gets spring expression parser.
     *
     * @return the spring expression parser
     */
    protected SpringELExpressionParser getSpringExpressionParser() {
        final SpelParserConfiguration configuration = new SpelParserConfiguration();
        final SpelExpressionParser spelExpressionParser = new SpelExpressionParser(configuration);
        final SpringELExpressionParser parser = new SpringELExpressionParser(spelExpressionParser,
                this.flowBuilderServices.getConversionService());

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

    /**
     * Create transition without a criteria.
     *
     * @param targetState the target state
     * @return the transition
     */
    protected Transition createTransition(final String targetState) {
        final DefaultTargetStateResolver resolver = new DefaultTargetStateResolver(targetState);
        return new Transition(resolver);
    }

    /**
     * Add end state backed by view.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     */
    protected void createEndState(final Flow flow, final String id, final String viewId) {
        createEndState(flow, id, new LiteralExpression(viewId));
    }

    /**
     * Add end state backed by view.
     *
     * @param flow       the flow
     * @param id         the id
     * @param expression the expression
     */
    protected void createEndState(final Flow flow, final String id, final Expression expression) {
        final ViewFactory viewFactory = this.flowBuilderServices.getViewFactoryCreator().createViewFactory(
                expression,
                this.flowBuilderServices.getExpressionParser(),
                this.flowBuilderServices.getConversionService(),
                null,
                this.flowBuilderServices.getValidator(),
                this.flowBuilderServices.getValidationHintResolver());

        createEndState(flow, id, viewFactory);
    }

    /**
     * Add end state backed by view.
     *
     * @param flow        the flow
     * @param id          the id
     * @param viewFactory the view factory
     */
    protected void createEndState(final Flow flow, final String id, final ViewFactory viewFactory) {
        try {
            final EndState endState = new EndState(flow, id);
            final Action finalResponseAction = new ViewFactoryActionAdapter(viewFactory);
            endState.setFinalResponseAction(finalResponseAction);
            logger.debug("Created end state state {} on flow id {}, backed by view factory {}", id, flow.getId(), viewFactory);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Add view state.
     *
     * @param flow       the flow
     * @param id         the id
     * @param expression the expression
     * @return the view state
     */
    protected ViewState createViewState(final Flow flow, final String id, final Expression expression) {
        try {
            final ViewFactory viewFactory = this.flowBuilderServices.getViewFactoryCreator().createViewFactory(
                    expression,
                    this.flowBuilderServices.getExpressionParser(),
                    this.flowBuilderServices.getConversionService(),
                    null,
                    this.flowBuilderServices.getValidator(),
                    this.flowBuilderServices.getValidationHintResolver());

            final ViewState viewState = new ViewState(flow, id, viewFactory);
            logger.debug("Added view state {}", viewState.getId());
            return viewState;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Add view state.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     * @return the view state
     */
    protected ViewState createViewState(final Flow flow, final String id, final String viewId) {
        return createViewState(flow, id, new LiteralExpression(viewId));
    }

    /**
     * Create subflow state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param subflow     the subflow
     * @param entryAction the entry action
     * @return the subflow state
     */
    protected SubflowState createSubflowState(final Flow flow, final String id, final String subflow,
                                              final Action entryAction) {

        final SubflowState state = new SubflowState(flow, id, new BasicSubflowExpression(subflow, this.loginFlowDefinitionRegistry));
        if (entryAction != null) {
            state.getEntryActionList().add(entryAction);
        }

        return state;
    }

    /**
     * Create mapper to subflow state.
     *
     * @param mappings the mappings
     * @return the mapper
     */
    protected Mapper createMapperToSubflowState(final List<DefaultMapping> mappings) {
        final DefaultMapper inputMapper = new DefaultMapper();
        for (final DefaultMapping mapping : mappings) {
            inputMapper.addMapping(mapping);
        }
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
    protected DefaultMapping createMappingToSubflowState(final String name, final String value,
                                                         final boolean required, final Class type) {

        final ExpressionParser parser = this.flowBuilderServices.getExpressionParser();

        final Expression source = parser.parseExpression(value, new FluentParserContext());
        final Expression target = parser.parseExpression(name, new FluentParserContext());

        final DefaultMapping mapping = new DefaultMapping(source, target);
        mapping.setRequired(required);

        final ConversionExecutor typeConverter =
                new RuntimeBindingConversionExecutor(type, this.flowBuilderServices.getConversionService());
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
    protected SubflowAttributeMapper createSubflowAttributeMapper(final Mapper inputMapper, final Mapper outputMapper) {
        return new GenericSubflowAttributeMapper(inputMapper, outputMapper);
    }
}
