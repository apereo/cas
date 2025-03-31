package org.apereo.cas.web.flow;

import org.apereo.cas.util.NamedObject;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.mapping.Mapping;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.RenderAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowConfigurer extends Ordered, NamedObject {

    /**
     * Empty actions array.
     */
    Action[] EMPTY_ACTIONS_ARRAY = {};

    /**
     * Flow id for risk-based authentication's verification.
     */
    String FLOW_ID_RISK_VERIFICATION = "riskauthverify";

    /**
     * Flow id for delegated authentication redirect.
     */
    String FLOW_ID_DELEGATION_REDIRECT = "clientredirect";

    /**
     * Flow id for password reset.
     */
    String FLOW_ID_PASSWORD_RESET = "pswdreset";

    /**
     * Flow id for account profiles.
     */
    String FLOW_ID_ACCOUNT = "account";

    /**
     * Main login flow id.
     */
    String FLOW_ID_LOGIN = "login";

    /**
     * Main logout flow id.
     */
    String FLOW_ID_LOGOUT = "logout";

    /**
     * Initialize.
     */
    void initialize();

    /**
     * Post initialization, typically after the state of application is declared as ready.
     *
     * @param applicationContext the application context
     */
    default void postInitialization(final ConfigurableApplicationContext applicationContext) {
    }

    /**
     * Gets login flow.
     *
     * @return the login flow
     */
    Flow getLoginFlow();

    /**
     * Gets logout flow.
     *
     * @return the logout flow
     */
    Flow getLogoutFlow();

    /**
     * Gets start state.
     *
     * @param flow the flow
     * @return the start state
     */
    TransitionableState getStartState(Flow flow);

    /**
     * Create transition transition.
     *
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    Transition createTransition(String criteriaOutcome, String targetState);

    /**
     * Create transition transition.
     *
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param actions         the actions
     * @return the transition
     */
    Transition createTransition(String criteriaOutcome, String targetState, Action... actions);

    /**
     * Create transition transition.
     *
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    Transition createTransition(String criteriaOutcome, TransitionableState targetState);

    /**
     * Create transition transition.
     *
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @param targetState               the target state
     * @param actions                   the actions
     * @return the transition
     */
    Transition createTransition(Expression criteriaOutcomeExpression, String targetState, Action... actions);

    /**
     * Create transition.
     *
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @param targetStateResolver       the target state resolver
     * @return the transition
     */
    Transition createTransition(String criteriaOutcomeExpression,
                                TargetStateResolver targetStateResolver);

    /**
     * Create transition.
     *
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @param targetStateResolver       the target state resolver
     * @return the transition
     */
    Transition createTransition(Expression criteriaOutcomeExpression,
                                TargetStateResolver targetStateResolver);

    /**
     * Create transition.
     *
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @param targetStateResolver       the target state resolver
     * @param actions                   the actions
     * @return the transition
     */
    Transition createTransition(Expression criteriaOutcomeExpression,
                                TargetStateResolver targetStateResolver,
                                Action... actions);

    /**
     * Create transition transition.
     *
     * @param targetState the target state
     * @return the transition
     */
    Transition createTransition(String targetState);

    /**
     * Create render action.
     *
     * @param fragmentExpression the fragment expression
     * @return the render action
     */
    RenderAction createRenderAction(String... fragmentExpression);

    /**
     * Create set action set action.
     *
     * @param name  the name
     * @param value the value
     * @return the set action
     */
    SetAction createSetAction(String name, String value);

    /**
     * Create evaluate action evaluate action.
     *
     * @param expression the expression
     * @return the evaluate action
     */
    EvaluateAction createEvaluateAction(String expression);

    /**
     * Create action state action state.
     *
     * @param flow the flow
     * @param name the name
     * @return the action state
     */
    ActionState createActionState(Flow flow, String name);

    /**
     * Create action state action state.
     *
     * @param flow   the flow
     * @param name   the name
     * @param action the action
     * @return the action state
     */
    ActionState createActionState(Flow flow, String name, String... action);

    /**
     * Create action state action state.
     *
     * @param flow    the flow
     * @param name    the name
     * @param actions the actions
     * @return the action state
     */
    ActionState createActionState(Flow flow, String name, Action... actions);

    /**
     * Create action state action state.
     *
     * @param flow   the flow
     * @param name   the name
     * @param action the action
     * @return the action state
     */
    ActionState createActionState(Flow flow, String name, Action action);

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
    DecisionState createDecisionState(Flow flow, String id, String testExpression,
                                      String thenStateId, String elseStateId);

    /**
     * Sets start state.
     *
     * @param flow  the flow
     * @param state the state
     */
    void setStartState(Flow flow, String state);


    /**
     * Sets start state.
     *
     * @param flow  the flow
     * @param state the state
     */
    void setStartState(Flow flow, TransitionableState state);


    /**
     * Create end state.
     *
     * @param flow the flow
     * @param id   the id
     * @return the end state
     */
    EndState createEndState(Flow flow, String id);

    /**
     * Create end state with option to handle an external redirect.
     *
     * @param flow     the flow
     * @param id       the id
     * @param viewId   the view id
     * @param redirect the redirect
     * @return the end state
     */
    EndState createEndState(Flow flow, String id, String viewId, boolean redirect);

    /**
     * Create end state.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     * @return the end state
     */
    EndState createEndState(Flow flow, String id, String viewId);

    /**
     * Create end state.
     *
     * @param flow       the flow
     * @param id         the id
     * @param expression the expression
     * @return the end state
     */
    EndState createEndState(Flow flow, String id, Expression expression);

    /**
     * Create end state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param viewFactory the view factory
     * @return the end state
     */
    EndState createEndState(Flow flow, String id, ViewFactory viewFactory);

    /**
     * Create view state view state.
     *
     * @param flow       the flow
     * @param id         the id
     * @param expression the expression
     * @param binder     the binder
     * @return the view state
     */
    ViewState createViewState(Flow flow, String id, Expression expression, BinderConfiguration binder);

    /**
     * Create view state view state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param viewFactory the view factory
     * @return the view state
     */
    ViewState createViewState(Flow flow, String id, ViewFactory viewFactory);

    /**
     * Create view state view state.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     * @return the view state
     */
    ViewState createViewState(Flow flow, String id, String viewId);

    /**
     * Create view state view state.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     * @param binder the binder
     * @return the view state
     */
    ViewState createViewState(Flow flow, String id, String viewId, BinderConfiguration binder);

    /**
     * Create end view state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param viewFactory the view factory
     * @return the view state
     */
    ViewState createEndViewState(Flow flow, String id, ViewFactory viewFactory);

    /**
     * Create end view state.
     *
     * @param flow   the flow
     * @param id     the id
     * @param viewId the view id
     * @return the view state
     */
    ViewState createEndViewState(Flow flow, String id, String viewId);

    /**
     * Create subflow state subflow state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param subflow     the subflow
     * @param entryAction the entry action
     * @return the subflow state
     */
    SubflowState createSubflowState(Flow flow, String id, String subflow,
                                    Action entryAction);

    /**
     * Create subflow state.
     *
     * @param flow        the flow
     * @param id          the id
     * @param subflow     the subflow
     * @param registry    the registry
     * @param entryAction the entry action
     * @return the subflow state
     */
    SubflowState createSubflowState(Flow flow, String id, String subflow,
                                    FlowDefinitionRegistry registry, Action entryAction);

    /**
     * Create subflow state subflow state.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @param subflow the subflow
     * @return the subflow state
     */
    default SubflowState createSubflowState(final Flow flow, final String stateId, final String subflow) {
        return createSubflowState(flow, stateId, subflow, null);
    }

    /**
     * Build flow.
     *
     * @param id the id
     * @return the flow
     */
    Flow buildFlow(String id);

    /**
     * Create state default transition.
     *
     * @param state       the state
     * @param targetState the target state
     */
    void createStateDefaultTransition(TransitionableState state, String targetState);

    /**
     * Create state default transition.
     *
     * @param state       the state
     * @param targetState the target state
     */
    void createStateDefaultTransition(TransitionableState state, StateDefinition targetState);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param attributes      the attributes
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        String targetState,
                                        Map<String, Object> attributes);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome);

    /**
     * Create transition for state transition.
     *
     * @param state               the state
     * @param criteriaOutcome     the criteria outcome
     * @param targetStateResolver the target state resolver
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        TargetStateResolver targetStateResolver);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        String targetState);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param attributes      the attributes
     * @param actions         the actions
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        String targetState,
                                        Map<String, Object> attributes,
                                        Action... actions);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param actions         the actions
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        String targetState,
                                        String... actions);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param actions         the actions
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state,
                                        String criteriaOutcome,
                                        String targetState,
                                        Action... actions);

    /**
     * Create transition for state transition.
     *
     * @param flow            the flow
     * @param stateId         the state id
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    Transition createTransitionForState(Flow flow, String stateId,
                                        String criteriaOutcome,
                                        String targetState);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param removeExisting  the remove existing
     * @param attributes      the attributes
     * @param actions         the actions
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state, String criteriaOutcome,
                                        String targetState, boolean removeExisting,
                                        Map<String, Object> attributes, Action... actions);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param removeExisting  the remove existing
     * @param attributes      the attributes
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state, String criteriaOutcome,
                                        String targetState, boolean removeExisting,
                                        Map<String, Object> attributes);

    /**
     * Create transition for state transition.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @param removeExisting  the remove existing
     * @return the transition
     */
    Transition createTransitionForState(TransitionableState state, String criteriaOutcome,
                                        String targetState, boolean removeExisting);

    /**
     * Insert transition for state.
     *
     * @param state           the state
     * @param criteriaOutcome the criteria outcome
     * @param targetState     the target state
     * @return the transition
     */
    Transition insertTransitionForState(TransitionableState state, String criteriaOutcome,
                                        String targetState);

    /**
     * Create expression expression.
     *
     * @param expression   the expression
     * @param expectedType the expected type
     * @return the expression
     */
    Expression createExpression(String expression, Class expectedType);

    /**
     * Create expression expression.
     *
     * @param expression the expression
     * @return the expression
     */
    Expression createExpression(String expression);

    /**
     * Contains flow state boolean.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return true/false
     */
    boolean containsFlowState(Flow flow, String stateId);

    /**
     * Contains subflow state boolean.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return true/false
     */
    boolean containsSubflowState(Flow flow, String stateId);

    /**
     * Contains transition boolean.
     *
     * @param state      the state
     * @param transition the transition
     * @return true/false
     */
    boolean containsTransition(TransitionableState state, String transition);

    /**
     * Create flow variable flow variable.
     *
     * @param flow the flow
     * @param id   the id
     * @param type the type
     * @return the flow variable
     */
    FlowVariable createFlowVariable(Flow flow, String id, Class type);

    /**
     * Create state binder configuration.
     *
     * @param properties the properties
     * @return the binder configuration
     */
    BinderConfiguration createStateBinderConfiguration(Map<String, Map<String, String>> properties);

    /**
     * Create state binder configuration binder configuration.
     *
     * @param properties the properties
     * @return the binder configuration
     */
    BinderConfiguration createStateBinderConfiguration(List<String> properties);

    /**
     * Create state model binding.
     *
     * @param state     the state
     * @param modelName the model name
     * @param modelType the model type
     */
    void createStateModelBinding(TransitionableState state, String modelName, Class modelType);

    /**
     * Gets view state binder configuration.
     *
     * @param state the state
     * @return the view state binder configuration
     */
    BinderConfiguration getViewStateBinderConfiguration(ViewState state);

    /**
     * Gets transition execution criteria chain for transition.
     *
     * @param def the def
     * @return the transition execution criteria chain for transition
     */
    List<TransitionCriteria> getTransitionExecutionCriteriaChainForTransition(Transition def);

    /**
     * Gets state.
     *
     * @param <T>     the type parameter
     * @param flow    the flow
     * @param stateId the state id
     * @param clazz   the clazz
     * @return the state
     */
    <T> T getState(Flow flow, String stateId, Class<T> clazz);

    /**
     * Gets state.
     *
     * @param flow    the flow
     * @param stateId the state id
     * @return the state
     */
    TransitionableState getState(Flow flow, String stateId);

    /**
     * Gets flow.
     *
     * @param id the id
     * @return the flow
     */
    Flow getFlow(String id);

    /**
     * Gets flow.
     *
     * @param registry the registry
     * @param id       the id
     * @return the flow
     */
    Flow getFlow(FlowDefinitionRegistry registry, String id);

    /**
     * Create mapping to a (subflow) state.
     *
     * @param targetExpression the target expression
     * @param sourceExpression the source expression
     * @param required         the required
     * @param type             the type
     * @return the default mapping
     */
    Mapping createFlowMapping(String sourceExpression, String targetExpression,
                              boolean required, Class type);

    /**
     * Gets transitionable state.
     *
     * @param <T>     the type parameter
     * @param flow    the flow
     * @param stateId the state id
     * @param clazz   the clazz
     * @return the transitionable state
     */
    <T extends TransitionableState> T getTransitionableState(Flow flow, String stateId, Class<T> clazz);

    /**
     * Create external redirect view factory.
     *
     * @param expressionId the expression id
     * @return the view factory
     */
    ViewFactory createExternalRedirectViewFactory(String expressionId);
}
