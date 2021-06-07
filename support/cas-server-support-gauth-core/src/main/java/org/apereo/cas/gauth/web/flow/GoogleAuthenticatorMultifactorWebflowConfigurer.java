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
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link GoogleAuthenticatorMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_GAUTH_EVENT_ID = "mfa-gauth";

    public GoogleAuthenticatorMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                           final FlowDefinitionRegistry flowDefinitionRegistry,
                                                           final ConfigurableApplicationContext applicationContext,
                                                           final CasConfigurationProperties casProperties,
                                                           final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry),
            mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, MFA_GAUTH_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, GoogleAuthenticatorTokenCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction("prepareGoogleAuthenticatorLoginAction"),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            setStartState(flow, initLoginFormState);

            val acctRegCheckState = createActionState(flow, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION,
                createEvaluateAction("googleAccountCheckRegistrationAction"));
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION);
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_CONFIRM, "viewConfirmRegistration");
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val acctRegSaveState = createActionState(flow, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION,
                createEvaluateAction("googleSaveAccountRegistrationAction"));
            createTransitionForState(acctRegSaveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            createStateDefaultTransition(acctRegSaveState, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("validateSelectedRegistrationAction"),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val propertiesToBind = CollectionUtils.wrapList("token", "accountId");
            val binder = createStateBinderConfiguration(propertiesToBind);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                "gauth/casGoogleAuthenticatorLoginView", binder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, GoogleAuthenticatorTokenCredential.class);
            viewLoginFormState.getEntryActionList().add(setPrincipalAction);

            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION,
                Map.of("bind", Boolean.FALSE, "validate", Boolean.FALSE));
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SELECT, "viewConfirmRegistration",
                Map.of("bind", Boolean.FALSE, "validate", Boolean.FALSE));

            val regViewState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION, "gauth/casGoogleAuthenticatorRegistrationView");
            regViewState.getEntryActionList().addAll(setPrincipalAction, createEvaluateAction("googleAccountCreateRegistrationAction"));
            createTransitionForState(regViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION);

            val confirmRegViewState = createViewState(flow, "viewConfirmRegistration", "gauth/casGoogleAuthenticatorConfirmRegistrationView");
            confirmRegViewState.getEntryActionList().add(setPrincipalAction);

            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_VIEW_REGISTRATION);
            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_DELETE, "googleAccountDeleteDevice");
            createTransitionForState(confirmRegViewState, CasWebflowConstants.TRANSITION_ID_SELECT,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                createEvaluateAction("googleAccountConfirmSelectionAction"));

            val deleteDeviceState = createActionState(flow, "googleAccountDeleteDevice", "googleAccountDeleteDeviceAction");
            createTransitionForState(deleteDeviceState, CasWebflowConstants.STATE_ID_SUCCESS, acctRegCheckState.getId());

        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_GAUTH_EVENT_ID,
            casProperties.getAuthn().getMfa().getGauth().getId());
    }
}
