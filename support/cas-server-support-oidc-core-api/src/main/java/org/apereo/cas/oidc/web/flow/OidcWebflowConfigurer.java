package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OidcWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Action oidcRegisteredServiceUIAction;

    public OidcWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                 final Action oidcRegisteredServiceUIAction, final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.oidcRegisteredServiceUIAction = oidcRegisteredServiceUIAction;
    }

    @Override
    protected void doInitialize() {
        val loginFlow = getLoginFlow();
        if (loginFlow != null) {
            val state = getTransitionableState(loginFlow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(this.oidcRegisteredServiceUIAction);
        }
    }
}
