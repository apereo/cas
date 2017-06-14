package org.apereo.cas.adaptors.swivel.web.flow;

import org.apereo.cas.web.flow.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SwivelMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_SWIVEL_EVENT_ID = "mfa-swivel";

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public SwivelMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_SWIVEL_EVENT_ID, this.flowDefinitionRegistry);
    }
}
