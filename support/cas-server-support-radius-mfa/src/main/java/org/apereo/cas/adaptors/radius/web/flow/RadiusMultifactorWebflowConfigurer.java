package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link RadiusMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /** Radius Webflow event id. */
    public static final String MFA_RADIUS_EVENT_ID = "mfa-radius";
    
    private final FlowDefinitionRegistry radiusFlowRegistry;

    public RadiusMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final FlowDefinitionRegistry radiusFlowRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.radiusFlowRegistry = radiusFlowRegistry;
    }

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_RADIUS_EVENT_ID, this.radiusFlowRegistry);
    }
}
