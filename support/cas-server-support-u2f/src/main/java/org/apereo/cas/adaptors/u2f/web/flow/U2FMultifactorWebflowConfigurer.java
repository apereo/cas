package org.apereo.cas.adaptors.u2f.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link U2FMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class U2FMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_U2F_EVENT_ID = "mfa-u2f";

    private final FlowDefinitionRegistry u2fFlowRegistry;

    public U2FMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                           final FlowDefinitionRegistry flowDefinitionRegistry,
                                           final ApplicationContext applicationContext,
                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.u2fFlowRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_U2F_EVENT_ID,
                this.u2fFlowRegistry, casProperties.getAuthn().getMfa().getU2f().getId());
    }
}
