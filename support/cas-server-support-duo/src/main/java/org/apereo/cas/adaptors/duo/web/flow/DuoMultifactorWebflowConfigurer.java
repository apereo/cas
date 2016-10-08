package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link DuoMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Duo Webflow event id. */
    public static final String MFA_DUO_EVENT_ID = "mfa-duo";

    private FlowDefinitionRegistry duoFlowRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_DUO_EVENT_ID, this.duoFlowRegistry);
    }
    
    public void setDuoFlowRegistry(final FlowDefinitionRegistry duoFlowRegistry) {
        this.duoFlowRegistry = duoFlowRegistry;
    }
}
