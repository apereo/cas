package org.apereo.cas.trusted.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                                 final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                                                                 final ConfigurableApplicationContext applicationContext,
                                                                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        super.doInitialize();
        val accountFlow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val accountView = getState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
        createTransitionForState(accountView, "deleteTrustedDevice", "deleteMultifactorTrustedDevice");
        val removeDevice = createActionState(accountFlow, "deleteMultifactorTrustedDevice",
            CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_MFA_TRUSTED_DEVICE);
        createTransitionForState(removeDevice, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(removeDevice, CasWebflowConstants.TRANSITION_ID_ERROR, accountFlow.getStartState().getId());
    }
}
