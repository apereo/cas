package org.apereo.cas.adaptors.swivel.web.flow;

import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SwivelMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public SwivelMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
