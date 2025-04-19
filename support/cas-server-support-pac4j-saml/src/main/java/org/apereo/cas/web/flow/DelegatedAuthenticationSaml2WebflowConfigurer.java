package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link DelegatedAuthenticationSaml2WebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j saml integration.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DelegatedAuthenticationSaml2WebflowConfigurer extends AbstractCasWebflowConfigurer {
    public DelegatedAuthenticationSaml2WebflowConfigurer(
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
            state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT));
        }
        createDelegatedClientLogoutAction();
    }

    protected void createDelegatedClientLogoutAction() {
        val logoutFlow = getLogoutFlow();
        if (logoutFlow != null) {
            val finishLogout = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, ActionState.class);
            finishLogout.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT));
            val terminateSessionState = getState(logoutFlow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
            terminateSessionState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_FINISH_LOGOUT));
            terminateSessionState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION));

            if (applicationContext.containsBean(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_IDP_RESTORE_SLO_REQUEST)) {
                val doLogout = getState(logoutFlow, CasWebflowConstants.STATE_ID_DO_LOGOUT, ActionState.class);
                doLogout.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_IDP_RESTORE_SLO_REQUEST));
            }
        }
    }
}
