package org.apereo.cas.adaptors.radius.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link RadiusMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RadiusMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /** Radius Webflow event id. */
    public static final String MFA_RADIUS_EVENT_ID = "mfa-radius";
    
    private final FlowDefinitionRegistry radiusFlowRegistry;

    public RadiusMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final FlowDefinitionRegistry radiusFlowRegistry,
                                              final ApplicationContext applicationContext,
                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.radiusFlowRegistry = radiusFlowRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_RADIUS_EVENT_ID,
                this.radiusFlowRegistry, casProperties.getAuthn().getMfa().getRadius().getId());
    }
}
