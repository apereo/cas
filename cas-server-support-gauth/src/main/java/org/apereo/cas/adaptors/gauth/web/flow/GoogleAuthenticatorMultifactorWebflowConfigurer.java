package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Webflow event id. */
    public static final String MFA_GAUTH_EVENT_ID = "mfa-gauth";
    
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_GAUTH_EVENT_ID, this.flowDefinitionRegistry);
    }

    public void setFlowDefinitionRegistry(final FlowDefinitionRegistry flowDefinitionRegistry) {
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }
}
