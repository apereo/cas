package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ConsentAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class ConsentAccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public ConsentAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry flowDefinitionRegistry,
                                                  final ConfigurableApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val flow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        if (flow != null) {
            val prepAction = createEvaluateAction(CasWebflowConstants.ACTION_ID_CONSENT_ACCOUNT_PROFILE_PREPARE);
            val accountView = getState(flow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
            accountView.getRenderActionList().add(prepAction);
        }
    }
}
