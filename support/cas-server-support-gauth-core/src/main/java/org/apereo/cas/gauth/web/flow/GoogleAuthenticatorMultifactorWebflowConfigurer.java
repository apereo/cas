package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    public GoogleAuthenticatorMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                           final FlowDefinitionRegistry flowDefinitionRegistry,
                                                           final FlowDefinitionRegistry mfaFlowDefinitionRegistry,
                                                           final ConfigurableApplicationContext applicationContext,
                                                           final CasConfigurationProperties casProperties,
                                                           final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, flowDefinitionRegistry,
            applicationContext, casProperties, Optional.of(mfaFlowDefinitionRegistry),
            mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        val providerId = casProperties.getAuthn().getMfa().getGauth().getId();

        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, providerId);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, GoogleAuthenticatorTokenCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_PREPARE_LOGIN),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            setStartState(flow, initLoginFormState);

            val acctRegCheckState = createActionState(flow, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_CHECK_ACCOUNT_REGISTRATION));
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION);
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_CONFIRM, "viewConfirmRegistration");
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_REGISTRATION_REQUIRED);

            createViewState(flow, CasWebflowConstants.STATE_ID_REGISTRATION_REQUIRED, "gauth/casGoogleAuthenticatorRegistrationRequiredView");

            val acctRegSaveState = createActionState(flow, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION));
            createTransitionForState(acctRegSaveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            createStateDefaultTransition(acctRegSaveState, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_SELECTED_REGISTRATION),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val propertiesToBind = CollectionUtils.wrapList("token", "accountId");
            val binder = createStateBinderConfiguration(propertiesToBind);
            val googleLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "gauth/casGoogleAuthenticatorLoginView", binder);
            createStateModelBinding(googleLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, GoogleAuthenticatorTokenCredential.class);
            googleLoginFormState.getEntryActionList().add(setPrincipalAction);

            createTransitionForState(googleLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, createTransitionAttributes(true, true));

            createTransitionForState(googleLoginFormState, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION,
                createTransitionAttributes(false, false));

            createTransitionForState(googleLoginFormState, CasWebflowConstants.TRANSITION_ID_CONFIRM, "validateGoogleAccountToken",
                createTransitionAttributes(false, false));

            createTransitionForState(googleLoginFormState, CasWebflowConstants.TRANSITION_ID_SELECT, "viewConfirmRegistration",
                createTransitionAttributes(false, false));

            val regViewState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION, "gauth/casGoogleAuthenticatorRegistrationView");
            regViewState.getEntryActionList().addAll(setPrincipalAction, createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION));
            createTransitionForState(regViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION);

            val confirmRegViewState = createViewState(flow, "viewConfirmRegistration", "gauth/casGoogleAuthenticatorConfirmRegistrationView");
            confirmRegViewState.getEntryActionList().add(setPrincipalAction);
            confirmRegViewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_GOOGLE_PREPARE_LOGIN));

            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_REGISTER,
                CasWebflowConstants.STATE_ID_VIEW_REGISTRATION, CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_REGISTRATION);
            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_DELETE, "googleAccountDeleteDevice");
            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_SELECT, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_SELECTION);

            val deleteDeviceState = createActionState(flow, "googleAccountDeleteDevice", CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_DELETE_DEVICE);
            createTransitionForState(deleteDeviceState, CasWebflowConstants.STATE_ID_SUCCESS, acctRegCheckState.getId());

            val confirmTokenState = createActionState(flow, "validateGoogleAccountToken", CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_TOKEN);
            createTransitionForState(confirmTokenState, CasWebflowConstants.TRANSITION_ID_SUCCESS, regViewState.getId());
            createTransitionForState(confirmTokenState, CasWebflowConstants.TRANSITION_ID_ERROR, googleLoginFormState.getId());
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), providerId, providerId);
    }
}
