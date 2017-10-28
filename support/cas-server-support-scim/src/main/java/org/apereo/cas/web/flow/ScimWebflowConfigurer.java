package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
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
    public ScimWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                 final ApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState tgtAction = getState(flow, CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET, ActionState.class);
            tgtAction.getExitActionList().add(createEvaluateAction("principalScimProvisionerAction"));
        }
    }
}
