package org.apereo.cas.adaptors.azure.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AzureAuthenticatorMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AzureAuthenticatorMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public AzureAuthenticatorMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                final boolean enableDeviceRegistration,
                                                                final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, enableDeviceRegistration);
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication(this.flowDefinitionRegistry);
    }
}
