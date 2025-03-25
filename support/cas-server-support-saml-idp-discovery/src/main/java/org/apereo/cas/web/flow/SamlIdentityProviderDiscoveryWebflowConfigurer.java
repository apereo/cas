package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SamlIdentityProviderDiscoveryWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class SamlIdentityProviderDiscoveryWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SamlIdentityProviderDiscoveryWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                          final FlowDefinitionRegistry flowDefinitionRegistry,
                                                          final ConfigurableApplicationContext applicationContext,
                                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        flow.getStartActionList().add(new ConsumerExecutionAction(requestContext ->
            requestContext.getFlowScope().put("identityProviderDiscoveryEnabled", Boolean.TRUE)));
    }
}
