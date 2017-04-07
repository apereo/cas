package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.web.flow.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link U2FMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_U2F_EVENT_ID = "mfa-u2f";

    private final FlowDefinitionRegistry u2fFlowRegistry;

    public U2FMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                           final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.u2fFlowRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_U2F_EVENT_ID, this.u2fFlowRegistry);
    }
}
