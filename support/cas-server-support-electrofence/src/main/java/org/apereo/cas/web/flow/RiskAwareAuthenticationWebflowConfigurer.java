package org.apereo.cas.web.flow;

import org.apereo.cas.impl.plans.BlockAuthenticationContingencyPlan;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link RiskAwareAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String VIEW_ID_BLOCKED_AUTHN = "casRiskAuthenticationBlockedView";

    public RiskAwareAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState submit = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
            createTransitionForState(submit, BlockAuthenticationContingencyPlan.EVENT_ID_BLOCK_AUTHN, VIEW_ID_BLOCKED_AUTHN);
            createViewState(flow, VIEW_ID_BLOCKED_AUTHN, VIEW_ID_BLOCKED_AUTHN);
        }
    }
}
