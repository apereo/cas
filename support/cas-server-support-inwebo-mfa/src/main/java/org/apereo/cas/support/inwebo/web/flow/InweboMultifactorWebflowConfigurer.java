package org.apereo.cas.support.inwebo.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

/**
 * The Inwebo webflow configurer.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public class InweboMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_INWEBO_EVENT_ID = "mfa-inwebo";

    public InweboMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                           final FlowDefinitionRegistry flowDefinitionRegistry,
                                                           final ConfigurableApplicationContext applicationContext,
                                                           final CasConfigurationProperties casProperties,
                                                           final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties, Optional.of(flowDefinitionRegistry),
                mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_INWEBO_EVENT_ID,
                casProperties.getAuthn().getMfa().getInwebo().getId());
    }
}
