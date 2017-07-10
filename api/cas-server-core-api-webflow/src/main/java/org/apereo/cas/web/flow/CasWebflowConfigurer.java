package org.apereo.cas.web.flow;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.ViewFactory;

/**
 * This is {@link CasWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowConfigurer {

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
     * @return the transition
     */
    Transition createTransition(String criteriaOutcome, TransitionableState targetState);

    /**
     * Create transition transition.
     *
     * @param criteriaOutcomeExpression the criteria outcome expression
     * @param targetState               the target state
     * @return the transition
     */
    Transition createTransition(Expression criteriaOutcomeExpression, String targetState);

    /**
     * Create transition transition.
     *
     * @param targetState the target state
     * @return the transition
     */
    Transition createTransition(String targetState);
    
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
     * @param flow    the flow
     * @param name    the name
     * @param actions the actions
     * @return the action state
     */
    ActionState createActionState(Flow flow, String name, Action... actions);

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
     * Create subflow state subflow state.
     *
     * @param flow    the flow
     * @param id      the id
     * @param subflow the subflow
     * @return the subflow state
     */
    SubflowState createSubflowState(Flow flow, String id, String subflow);


    /**
     * Build flow.
     *
     * @param location the location
     * @param id       the id
     * @return the flow
     */
    Flow buildFlow(String location, String id);
}
