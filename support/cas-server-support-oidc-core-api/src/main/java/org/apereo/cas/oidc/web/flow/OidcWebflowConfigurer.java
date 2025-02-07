package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link OidcWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public OidcWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry flowDefinitionRegistry,
                                 final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val loginFlow = getLoginFlow();
        if (loginFlow != null) {
            val state = getTransitionableState(loginFlow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_OIDC_REGISTERED_SERVICE_UI));
        }
    }

    @Override
    public void postInitialization(final ConfigurableApplicationContext applicationContext) {
        val flow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        if (flow != null) {
            val prepAction = createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_ACCESS_TOKENS);
            val accountView = getState(flow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
            accountView.getRenderActionList().add(prepAction);
        }
    }
}
