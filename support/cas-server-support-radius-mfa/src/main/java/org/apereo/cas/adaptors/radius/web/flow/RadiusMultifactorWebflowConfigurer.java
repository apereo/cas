package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.Optional;

/**
 * This is {@link RadiusMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Radius Webflow event id.
     */
    public static final String MFA_RADIUS_EVENT_ID = "mfa-radius";

    public RadiusMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final FlowDefinitionRegistry radiusFlowRegistry,
                                              final ConfigurableApplicationContext applicationContext,
                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties, Optional.of(radiusFlowRegistry));
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_RADIUS_EVENT_ID,
            casProperties.getAuthn().getMfa().getRadius().getId());
    }
}
