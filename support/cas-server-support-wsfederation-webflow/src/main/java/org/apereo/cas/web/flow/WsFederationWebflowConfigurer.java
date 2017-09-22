package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String WS_FEDERATION_ACTION = "wsFederationAction";
    private static final String WS_FEDERATION_REDIRECT = "wsFederationRedirect";

    private boolean autoRedirect = true;

    public WsFederationWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                         final boolean redirect) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.autoRedirect = redirect;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createEndState(flow, WS_FEDERATION_REDIRECT, "flowScope.WsFederationIdentityProviderUrl", true);
            final ActionState actionState = createActionState(flow, WS_FEDERATION_ACTION, createEvaluateAction(WS_FEDERATION_ACTION));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, WS_FEDERATION_REDIRECT));

            if (this.autoRedirect) {
                setStartState(flow, actionState);
            }
        }
    }
}
