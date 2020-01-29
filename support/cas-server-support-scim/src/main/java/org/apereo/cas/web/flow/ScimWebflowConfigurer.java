package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
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
                                 final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val tgtAction = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            tgtAction.getExitActionList().add(createEvaluateAction("principalScimProvisionerAction"));
        }
    }
}
