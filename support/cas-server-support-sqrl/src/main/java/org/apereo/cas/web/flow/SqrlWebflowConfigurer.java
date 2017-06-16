package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SqrlWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SqrlWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        
    }
}
