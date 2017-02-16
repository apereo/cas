package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link X509WebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for x509 integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class X509WebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String EVENT_ID_START_X509 = "startX509Authenticate";

    public X509WebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState actionState = createActionState(flow, EVENT_ID_START_X509, createEvaluateAction("x509Check"));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                    CasWebflowConstants.TRANSITION_ID_WARN));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR,
                    CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);

            final ActionState state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, EVENT_ID_START_X509, true);
        }
    }
}
