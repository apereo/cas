package org.jasig.cas.adaptors.radius.web.flow;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link RadiusMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("radiusMultifactorWebflowConfigurer")
public class RadiusMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /** Radius Webflow event id. */
    public static final String MFA_RADIUS_EVENT_ID = "mfa-radius";

    @Autowired
    @Qualifier("radiusFlowRegistry")
    private FlowDefinitionRegistry radiusFlowRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_RADIUS_EVENT_ID, this.radiusFlowRegistry);
    }
}
