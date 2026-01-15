package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link PasswordManagementAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class PasswordManagementAccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public PasswordManagementAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                             final FlowDefinitionRegistry flowDefinitionRegistry,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 100);
    }

    @Override
    protected void doInitialize() {
        val flow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        if (flow != null) {
            val prepAction = createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT);
            flow.getStartActionList().add(prepAction);

            val accountView = getState(flow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
            accountView.getRenderActionList().add(prepAction);
        }
    }
}
