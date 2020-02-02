package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.Optional;

/**
 * This is {@link AccepttoMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AccepttoMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public AccepttoMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                     final boolean deviceRegistrationEnabled,
                                                     final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                     final ConfigurableApplicationContext applicationContext,
                                                     final CasConfigurationProperties casProperties,
                                                     final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, deviceRegistrationEnabled,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry));
    }

    @Override
    protected void doInitialize() {
        registerMultifactorTrustedAuthentication();
    }
}

