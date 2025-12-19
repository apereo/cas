package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link RiskAuthenticationVerificationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RiskAuthenticationVerificationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public RiskAuthenticationVerificationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                           final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                                           final ConfigurableApplicationContext applicationContext,
                                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val riskVerificationFlow = buildFlow(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION);
        val checkRiskTokenState = createActionState(riskVerificationFlow,
            CasWebflowConstants.STATE_ID_RISK_AUTHENTICATION_TOKEN_CHECK,
            CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK);
        val confirmationView = createEndState(riskVerificationFlow, "riskVerificationConfirmationView", "adaptive-authn/casRiskAuthenticationVerifiedView");
        createStateDefaultTransition(checkRiskTokenState, confirmationView.getId());
        riskVerificationFlow.setStartState(checkRiskTokenState);
        flowDefinitionRegistry.registerFlowDefinition(riskVerificationFlow);
    }
}
