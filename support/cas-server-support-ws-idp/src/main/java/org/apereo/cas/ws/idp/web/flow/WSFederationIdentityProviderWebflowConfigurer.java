package org.apereo.cas.ws.idp.web.flow;

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
 * This is {@link WSFederationIdentityProviderWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WSFederationIdentityProviderWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Action wsfedAction;

    public WSFederationIdentityProviderWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                         final Action wsfedAction, final ConfigurableApplicationContext applicationContext,
                                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.wsfedAction = wsfedAction;
    }

    @Override
    protected void doInitialize() {
        val loginFlow = getLoginFlow();
        if (loginFlow != null) {
            val state = getTransitionableState(loginFlow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(this.wsfedAction);
        }
    }
}
