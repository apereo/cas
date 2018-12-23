package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;

import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasSimpleMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CasSimpleMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_SIMPLE_EVENT_ID = "mfa-simple";

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public CasSimpleMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                 final FlowDefinitionRegistry flowDefinitionRegistry,
                                                 final ApplicationContext applicationContext,
                                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_SIMPLE_EVENT_ID,
                this.flowDefinitionRegistry, casProperties.getAuthn().getMfa().getSimple().getId());
    }
}
