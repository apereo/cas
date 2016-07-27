package org.apereo.cas.web.flow;

import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;

/**
 * The {@link SpengoWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for spnego integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SpengoWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String SPNEGO = "spnego";

    private static final String START_SPNEGO_AUTHENTICATE = "startSpnegoAuthenticate";
    
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, START_SPNEGO_AUTHENTICATE, createEvaluateAction("negociateSpnego"));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, SPNEGO));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, 
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));

        final ActionState spnego = createActionState(flow, SPNEGO, createEvaluateAction(SPNEGO));
        spnego.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        spnego.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow)));

        final ActionState evaluateClientRequest = createActionState(flow, "evaluateClientRequest",
                createEvaluateAction(casProperties.getAuthn().getSpnego().getHostNameClientActionStrategy()));
        evaluateClientRequest.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_YES, START_SPNEGO_AUTHENTICATE));
        evaluateClientRequest.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_NO, getStartState(flow)));

    }
}
