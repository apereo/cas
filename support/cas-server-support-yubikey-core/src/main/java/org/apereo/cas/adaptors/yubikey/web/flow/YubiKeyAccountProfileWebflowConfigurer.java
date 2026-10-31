package org.apereo.cas.adaptors.yubikey.web.flow;

import module java.base;
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
 * This is {@link YubiKeyAccountProfileWebflowConfigurer}.
 * Lets the account profile register a YubiKey, reusing the registration view and save action of the login flow.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class YubiKeyAccountProfileWebflowConfigurer extends MultifactorAuthenticationAccountProfileWebflowConfigurer {
    /**
     * Transition from the account profile to the YubiKey registration view.
     */
    public static final String TRANSITION_ID_REGISTER_YUBIKEY = "registerYubiKey";

    private static final String STATE_ID_SAVE_REGISTRATION = "yubiKeyAccountProfileSaveRegistration";

    public YubiKeyAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
        myAccountView.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_YUBIKEY_MFA_PREPARE));

        val regViewState = createViewState(accountFlow, CasWebflowConstants.STATE_ID_YUBIKEY_VIEW_REGISTRATION, "yubikey/casYubiKeyRegistrationView");
        regViewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_YUBIKEY_MFA_REGISTRATION));
        regViewState.getEntryActionList().add(createSetAction("viewScope.principal", "conversationScope.authentication.principal"));
        createTransitionForState(regViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_SAVE_REGISTRATION);

        val saveState = createActionState(accountFlow, STATE_ID_SAVE_REGISTRATION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_YUBIKEY_SAVE_ACCOUNT_REGISTRATION));
        createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_YUBIKEY_REGISTRATION_FINALIZED);
        createStateDefaultTransition(saveState, regViewState.getId());

        val finalizeRegistrationAction = new PopulateMessageContextAction.Info("screen.account.mfadevices.register.success")
            .setEventId(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val finalizeState = createActionState(accountFlow,
            CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_YUBIKEY_REGISTRATION_FINALIZED, finalizeRegistrationAction);
        createStateDefaultTransition(finalizeState, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);

        createTransitionForState(myAccountView, TRANSITION_ID_REGISTER_YUBIKEY, regViewState.getId());
    }
}
