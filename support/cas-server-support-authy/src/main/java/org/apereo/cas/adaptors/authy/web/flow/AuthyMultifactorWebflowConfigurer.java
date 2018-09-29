package org.apereo.cas.adaptors.authy.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AuthyMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class AuthyMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /** Webflow event id. */
    public static final String MFA_AUTHY_EVENT_ID = "mfa-authy";
    
    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public AuthyMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                             final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                             final FlowDefinitionRegistry flowDefinitionRegistry,
                                             final ApplicationContext applicationContext,
                                             final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_AUTHY_EVENT_ID,
                this.flowDefinitionRegistry, casProperties.getAuthn().getMfa().getAuthy().getId());
    }
}
