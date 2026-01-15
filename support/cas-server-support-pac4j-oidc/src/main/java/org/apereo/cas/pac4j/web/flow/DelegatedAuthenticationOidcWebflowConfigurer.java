package org.apereo.cas.pac4j.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link DelegatedAuthenticationOidcWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j OIDC integration.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DelegatedAuthenticationOidcWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public DelegatedAuthenticationOidcWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPac4j().getWebflow().getOrder() + 10);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT, ActionState.class);
            state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_OIDC_CLIENT_LOGOUT));
        }
    }
}
