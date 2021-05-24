package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.plans.BlockAuthenticationContingencyPlan;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link RiskAwareAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String STATE_ID_BLOCKED_AUTHN = "riskyAuthenticationBlocked";

    public RiskAwareAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val submit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
            createTransitionForState(submit, BlockAuthenticationContingencyPlan.EVENT_ID_BLOCK_AUTHN, STATE_ID_BLOCKED_AUTHN);
            createViewState(flow, STATE_ID_BLOCKED_AUTHN, "adaptive-authn/casRiskAuthenticationBlockedView");
        }
    }
}
