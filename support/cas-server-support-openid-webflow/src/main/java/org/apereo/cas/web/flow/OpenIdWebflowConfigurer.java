package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link OpenIdWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for openid integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String OPEN_ID_SINGLE_SIGN_ON_ACTION = "openIdSingleSignOnAction";

    public OpenIdWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            final String condition = getOpenIdModeCondition();

            final DecisionState decisionState = createDecisionState(flow, "selectFirstAction",
                    condition, OPEN_ID_SINGLE_SIGN_ON_ACTION,
                    getStartState(flow).getId());

            final ActionState actionState = createActionState(flow, OPEN_ID_SINGLE_SIGN_ON_ACTION,
                    createEvaluateAction(OPEN_ID_SINGLE_SIGN_ON_ACTION));

            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                    CasWebflowConstants.TRANSITION_ID_WARN));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);

            setStartState(flow, decisionState);
        }
    }

    private static String getOpenIdModeCondition() {
        return "externalContext.requestParameterMap['openid.mode'] ne '' "
                + "&& externalContext.requestParameterMap['openid.mode'] ne null "
                + "&& externalContext.requestParameterMap['openid.mode'] ne 'associate'";
    }
}
