package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link GoogleAuthenticatorMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public GoogleAuthenticatorMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                final boolean enableDeviceRegistration,
                                                                final FlowDefinitionRegistry flowDefinitionRegistry,
                                                                final ConfigurableApplicationContext applicationContext,
                                                                final CasConfigurationProperties casProperties,
                                                                final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, enableDeviceRegistration,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        registerMultifactorTrustedAuthentication();
    }
}
