package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
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
                                                          final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                          final ConfigurableApplicationContext applicationContext,
                                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            flow.getStartActionList().add(requestContext -> {
                requestContext.getFlowScope().put("identityProviderDiscoveryEnabled", Boolean.TRUE);
                return null;
            });
        }
    }
}
