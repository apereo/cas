package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link GoogleAuthenticatorMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GoogleAuthenticatorMultifactorTrustWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthenticationIntoWebflow(flowDefinitionRegistry, 
                GoogleAuthenticatorMultifactorWebflowConfigurer.MFA_GAUTH_EVENT_ID);
    }

    public void setFlowDefinitionRegistry(final FlowDefinitionRegistry flowDefinitionRegistry) {
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }
}
