package org.apereo.cas.adaptors.yubikey.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link YubiKeyMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class YubiKeyMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public YubiKeyMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final boolean deviceRegistrationEnabled,
                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    final ApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, deviceRegistrationEnabled, applicationContext, casProperties);
        flowDefinitionRegistry = loginFlowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorTrustedAuthentication(this.flowDefinitionRegistry);
    }
}

