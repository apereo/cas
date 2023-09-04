package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link AuthyMultifactorTrustedDeviceWebflowConfigurer}.
 *
 * @author Jérémie POISSON
 * 
 */
public class AuthyMultifactorTrustedDeviceWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public AuthyMultifactorTrustedDeviceWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        registerMultifactorTrustedAuthentication();
    }
}
