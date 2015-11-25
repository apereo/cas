package org.jasig.cas.web.flow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;

/**
 * The {@link SpengoWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for spnego integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("spnegoWebflowConfigurer")
public class SpengoWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Value("${cas.spnego.hostname.client.action.strategy:hostnameSpnegoClientAction}")
    private String hostNameClientActionStrategy;

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, "startSpnegoAuthenticate",
                createEvaluateAction("negociateSpnego"));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, "spnego"));

        final ActionState spnego = createActionState(flow, "spnego",
                createEvaluateAction("spnego"));
        spnego.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        spnego.getTransitionSet().add(createTransition(TRANSITION_ID_ERROR, getStartState(flow)));

        final ActionState evaluateClientRequest = createActionState(flow, "evaluateClientRequest",
                createEvaluateAction("hostNameClientActionStrategy"));
        evaluateClientRequest.getTransitionSet().add(createTransition(TRANSITION_ID_YES, "startSpnegoAuthenticate"));
        evaluateClientRequest.getTransitionSet().add(createTransition(TRANSITION_ID_NO, getStartState(flow)));

    }
}
