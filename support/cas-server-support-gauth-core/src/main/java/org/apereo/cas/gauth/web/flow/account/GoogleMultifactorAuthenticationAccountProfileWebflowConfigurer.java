package org.apereo.cas.gauth.web.flow.account;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.PopulateMessageContextAction;
import org.apereo.cas.web.flow.configurer.MultifactorAuthenticationAccountProfileWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GoogleMultifactorAuthenticationAccountProfileWebflowConfigurer extends MultifactorAuthenticationAccountProfileWebflowConfigurer {
    public GoogleMultifactorAuthenticationAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
        myAccountView.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_PREPARE));

        val regViewState = createViewState(accountFlow, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION, "gauth/casGoogleAuthenticatorRegistrationView");
        regViewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_REGISTRATION));
        regViewState.getEntryActionList().addAll(createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION));
        createTransitionForState(regViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_GOOGLE_SAVE_REGISTRATION);

        val acctRegSaveState = createActionState(accountFlow, CasWebflowConstants.STATE_ID_GOOGLE_SAVE_REGISTRATION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION));
        createTransitionForState(acctRegSaveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_GOOGLE_REGISTRATION_FINALIZED);
        createStateDefaultTransition(acctRegSaveState, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION);
        val finalizeRegistrationAction = new PopulateMessageContextAction.Info("screen.account.mfadevices.register.success")
            .setEventId(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val acctRegFinalize = createActionState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_GOOGLE_REGISTRATION_FINALIZED, finalizeRegistrationAction);
        createStateDefaultTransition(acctRegFinalize, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);

        val accountProfileView = (ViewState) accountFlow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(accountProfileView, "registerGauth", regViewState.getId());
    }
}
