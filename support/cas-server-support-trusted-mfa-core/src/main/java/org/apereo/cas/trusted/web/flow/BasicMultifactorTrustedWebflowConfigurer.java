package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link BasicMultifactorTrustedWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class BasicMultifactorTrustedWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public BasicMultifactorTrustedWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry flowDefinitionRegistry,
                                                    final FlowDefinitionRegistry mfaFlowDefinitionRegistry,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties,
                                                    final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, flowDefinitionRegistry,
            applicationContext, casProperties, Optional.of(mfaFlowDefinitionRegistry), mfaFlowCustomizers);
    }
}

