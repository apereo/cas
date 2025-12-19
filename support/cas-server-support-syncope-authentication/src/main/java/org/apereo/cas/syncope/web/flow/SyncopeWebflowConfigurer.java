package org.apereo.cas.syncope.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SyncopeWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SyncopeWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SyncopeWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry flowDefinitionRegistry,
                                    final ConfigurableApplicationContext applicationContext,
                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val tgtAction = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            tgtAction.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SYNCOPE_PRINCIPAL_PROVISIONER_ACTION));
        }
    }
}
