package org.apereo.cas.webauthn.web.flow.account;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.MultifactorAuthenticationAccountProfileWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link WebAuthnMultifactorAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WebAuthnMultifactorAccountProfileWebflowConfigurer extends MultifactorAuthenticationAccountProfileWebflowConfigurer {
    public WebAuthnMultifactorAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                              final FlowDefinitionRegistry accountProfileFlowRegistry,
                                                              final ConfigurableApplicationContext applicationContext,
                                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, accountProfileFlowRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        super.doInitialize();
        val accountFlow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);

        val myAccountView = getState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
        myAccountView.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE));
        
        val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
        val regViewState = createViewState(accountFlow, CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION, "webauthn/casWebAuthnRegistrationView");
        regViewState.getEntryActionList().addAll(
            setPrincipalAction,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT),
            createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN),
            createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_REGISTRATION),
            createEvaluateAction(CasWebflowConstants.ACTION_ID_WEB_AUTHN_START_REGISTRATION));
        createTransitionForState(regViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION);

        val accountProfileView = (ViewState) accountFlow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(accountProfileView, "registerWebAuthn", regViewState.getId());
    }
}
