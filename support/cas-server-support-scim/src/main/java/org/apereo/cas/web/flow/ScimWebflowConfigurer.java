package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ScimWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScimWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public ScimWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState tgtAction = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET);
            tgtAction.getExitActionList().add(createEvaluateAction("principalScimProvisionerAction"));
        }
    }
}
