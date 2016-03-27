package org.jasig.cas.adaptors.gauth.web.flow;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("googleAuthenticatorMultifactorWebflowConfigurer")
public class GoogleAuthenticatorMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Webflow event id. */
    public static final String MFA_GAUTH_EVENT_ID = "mfa-gauth";

    @Autowired
    @Qualifier("googleAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_GAUTH_EVENT_ID, this.flowDefinitionRegistry);
    }
}
