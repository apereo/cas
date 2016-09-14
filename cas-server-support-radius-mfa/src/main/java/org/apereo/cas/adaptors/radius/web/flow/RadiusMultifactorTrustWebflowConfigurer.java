package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link RadiusMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RadiusMultifactorTrustWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthenticationIntoWebflow(flowDefinitionRegistry, RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID);
    }

    public void setFlowDefinitionRegistry(final FlowDefinitionRegistry flowDefinitionRegistry) {
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }
}
