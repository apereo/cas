package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.Optional;

/**
 * This is {@link CasSimpleMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CasSimpleMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public CasSimpleMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                      final boolean enableDeviceRegistration,
                                                      final FlowDefinitionRegistry flowDefinitionRegistry,
                                                      final ConfigurableApplicationContext applicationContext,
                                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, enableDeviceRegistration,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry));
    }

    @Override
    protected void doInitialize() {
        registerMultifactorTrustedAuthentication();
    }
}
