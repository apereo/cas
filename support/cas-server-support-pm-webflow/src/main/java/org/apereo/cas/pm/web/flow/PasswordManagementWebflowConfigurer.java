package org.apereo.cas.pm.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.WeakPasswordException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.StringToCharArrayConverter;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This is {@link PasswordManagementWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Flow id for password reset.
     */
    public static final String FLOW_VAR_ID_PASSWORD = "password";

    public PasswordManagementWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                               final ConfigurableApplicationContext applicationContext,
                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        createAccountStatusViewStates(flow);
    }

    @Override
    public void postInitialization(final ConfigurableApplicationContext applicationContext) {
        val pm = casProperties.getAuthn().getPm();
        val flow = getLoginFlow();
        if (pm.getCore().isEnabled()) {
            val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            val sendAccountInfoState = getState(flow, CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS, ActionState.class);
            providerMap.forEach((id, provider) -> {
                if (containsSubflowState(flow, provider.getId())) {
                    val mfaSubflowState = getState(flow, provider.getId(), SubflowState.class);
                    createTransitionForState(mfaSubflowState, CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD,
                        CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);
                    createTransitionForState(sendAccountInfoState, provider.getId(), mfaSubflowState.getId());
                }
            });
        }
    }

    private void createAccountStatusViewStates(final Flow flow) {
        enablePasswordManagementForFlow(flow);

        createViewState(flow, CasWebflowConstants.STATE_ID_AUTHENTICATION_BLOCKED, "login-error/casAuthenticationBlockedView");
        createViewState(flow, CasWebflowConstants.STATE_ID_INVALID_WORKSTATION, "login-error/casBadWorkstationView");
        createViewState(flow, CasWebflowConstants.STATE_ID_INVALID_AUTHENTICATION_HOURS, "login-error/casBadHoursView");
        createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS, "password-reset/casPasswordUpdateSuccessView");
        createViewState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED, "login-error/casAccountLockedView");
        createViewState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED, "login-error/casAccountDisabledView");
        
        val pm = casProperties.getAuthn().getPm();
        if (pm.getCore().isEnabled()) {
            configurePasswordManagementWebflow(flow);
        } else {
            val expiredState = createViewState(flow, CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD, "login-error/casExpiredPassView");
            expiredState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE));
            val mustChangeState = createViewState(flow, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD, "login-error/casMustChangePassView");
            mustChangeState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE));
        }
    }

    private void configurePasswordManagementWebflow(final Flow flow) {
        configurePasswordResetFlow(flow, CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD, "login-error/casExpiredPassView");
        configurePasswordResetFlow(flow, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD, "login-error/casMustChangePassView");
        configurePasswordMustChangeForAuthnWarnings(flow);
        configurePasswordExpirationWarning(flow);
        createPasswordResetFlow();

        val startState = (ActionState) flow.getStartState();
        prependActionsToActionStateExecutionList(flow, startState.getId(), CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_TOKEN);

        createTransitionForState(startState, CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD,
            CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);
        createTransitionForState(startState, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD,
            CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW);
        createTransitionForState(startState, CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN,
            CasWebflowConstants.STATE_ID_PASSWORD_RESET_ERROR_VIEW);
        createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_ERROR_VIEW,
            "password-reset/casResetPasswordErrorView");

        val accountLockedState = getState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED, ViewState.class);
        val accountDisabledState = getState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED, ViewState.class);

        val enableUnlockAction = createSetAction("viewScope.enableAccountUnlock", "true");
        Stream.of(accountLockedState, accountDisabledState).forEach(state -> {
            state.getRenderActionList().add(enableUnlockAction);
            state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_UNLOCK_PREPARE));
        });

        val unlockedView = createEndState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_UNLOCKED, "login-error/casAccountUnlockedView");
        createTransitionForState(accountLockedState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "unlockAccountStatus");
        val unlockAction = createActionState(flow, "unlockAccountStatus", CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS);
        createTransitionForState(unlockAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, unlockedView.getId());
        createTransitionForState(unlockAction, CasWebflowConstants.TRANSITION_ID_ERROR, accountLockedState.getId());

        createTransitionForState(accountDisabledState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "enableAccountStatus");
        val enableAction = createActionState(flow, "enableAccountStatus", CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS);
        createTransitionForState(enableAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, unlockedView.getId());
        createTransitionForState(enableAction, CasWebflowConstants.TRANSITION_ID_ERROR, accountDisabledState.getId());

        val authnFailure = getState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE, ActionState.class);
        insertTransitionForState(authnFailure, WeakPasswordException.class.getSimpleName(), "weakPasswordDetectedView");
        val weakPasswordView =createViewState(flow, "weakPasswordDetectedView", "password-reset/casWeakPasswordDetectedView");
        createTransitionForState(weakPasswordView, CasWebflowConstants.TRANSITION_ID_CONTINUE, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
    }

    private void configurePasswordExpirationWarning(final Flow flow) {
        val warningState = getTransitionableState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        warningState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS));
    }

    private void configurePasswordMustChangeForAuthnWarnings(final Flow flow) {
        val warningState = getTransitionableState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        warningState.getEntryActionList().add(createEvaluateAction("flowScope.pswdChangePostLogin=true"));
        createTransitionForState(warningState, "changePassword", CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
    }

    private void createPasswordResetFlow() {
        val flow = getLoginFlow();
        if (flow != null) {
            val autoLogin = casProperties.getAuthn().getPm().getCore().isAutoLogin();

            val state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD,
                CasWebflowConstants.STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO);

            val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO,
                "password-reset/casResetPasswordSendInstructionsView");
            createTransitionForState(viewState, "findAccount", CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);

            val sendAccountInfoState = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS,
                CasWebflowConstants.ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS);

            createTransitionForState(sendAccountInfoState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_SENT_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(sendAccountInfoState, CasWebflowConstants.TRANSITION_ID_ERROR, viewState.getId());
            createViewState(flow, CasWebflowConstants.STATE_ID_SENT_RESET_PASSWORD_ACCT_INFO,
                "password-reset/casResetPasswordSentInstructionsView");

            registerPasswordResetFlowDefinition();

            val initializeLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            val originalTargetState = initializeLoginFormState.getTransition(CasWebflowConstants.STATE_ID_SUCCESS).getTargetStateId();
            val pswdResetSubFlowState = createSubflowState(flow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW, FLOW_ID_PASSWORD_RESET);

            val createTgt = getTransitionableState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
            val setAction = createEvaluateAction(String.join(PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD,
                "flowScope.", " = requestParameters.", " != null"));
            createTgt.getEntryActionList().add(setAction);

            createDecisionState(flow, CasWebflowConstants.DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION,
                "requestParameters."
                    + PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN
                    + " != null", CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW, originalTargetState);

            createTransitionForState(initializeLoginFormState,
                CasWebflowConstants.STATE_ID_SUCCESS,
                CasWebflowConstants.DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION, true);

            val redirect = createActionState(flow, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN, StaticEventExecutionAction.SUCCESS);
            createStateDefaultTransition(redirect, flow.getStartState().getId());

            createTransitionForState(
                pswdResetSubFlowState,
                CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE,
                autoLogin ? CasWebflowConstants.STATE_ID_REAL_SUBMIT : CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

            createDecisionState(flow,
                CasWebflowConstants.STATE_ID_CHECK_DO_CHANGE_PASSWORD,
                "flowScope." + PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD + " == true",
                CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD,
                createTgt.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId())
                .getEntryActionList().add(createEvaluateAction("flowScope.pswdChangePostLogin=true"));

            createTransitionForState(createTgt,
                CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_DO_CHANGE_PASSWORD, true);

            createDecisionState(flow,
                CasWebflowConstants.STATE_ID_POST_LOGIN_PASSWORD_CHANGE_CHECK,
                "flowScope.pswdChangePostLogin == true",
                getTransitionableState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS)
                    .getTransition(CasWebflowConstants.TRANSITION_ID_PROCEED).getTargetStateId(),
                autoLogin ? CasWebflowConstants.STATE_ID_REAL_SUBMIT : CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

            createTransitionForState(
                getTransitionableState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS),
                CasWebflowConstants.TRANSITION_ID_PROCEED,
                CasWebflowConstants.STATE_ID_POST_LOGIN_PASSWORD_CHANGE_CHECK);
        }
    }

    private void registerPasswordResetFlowDefinition() {
        val pswdFlow = buildFlow(FLOW_ID_PASSWORD_RESET);

        pswdFlow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

        val initReset = createActionState(pswdFlow, CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET,
            CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT);
        createStateDefaultTransition(initReset, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);

        val verifyQuestions = createActionState(pswdFlow, CasWebflowConstants.STATE_ID_VERIFY_SECURITY_QUESTIONS,
            CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS);
        createTransitionForState(verifyQuestions, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET);
        createTransitionForState(verifyQuestions, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_PASSWORD_RESET_ERROR_VIEW);

        val verifyRequest = createActionState(pswdFlow, CasWebflowConstants.STATE_ID_VERIFY_PASSWORD_RESET_REQUEST,
            CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST);
        createTransitionForState(verifyRequest, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SECURITY_QUESTIONS_VIEW);
        createTransitionForState(verifyRequest, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_PASSWORD_RESET_ERROR_VIEW);
        createTransitionForState(verifyRequest, "questionsDisabled", CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET);

        val questionsView = createViewState(pswdFlow, CasWebflowConstants.STATE_ID_SECURITY_QUESTIONS_VIEW,
            "password-reset/casResetPasswordVerifyQuestionsView");
        createTransitionForState(questionsView, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            CasWebflowConstants.STATE_ID_VERIFY_SECURITY_QUESTIONS,
            Map.of("bind", Boolean.FALSE, "validate", Boolean.FALSE));

        enablePasswordManagementForFlow(pswdFlow);

        createViewState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_ERROR_VIEW,
            "password-reset/casResetPasswordErrorView");
        createViewState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS,
            "password-reset/casPasswordUpdateSuccessView");
        configurePasswordResetFlow(pswdFlow, CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD,
            "login-error/casMustChangePassView");
        pswdFlow.setStartState(verifyRequest);
        mainFlowDefinitionRegistry.registerFlowDefinition(pswdFlow);

        createEndState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
        createTransitionForState(
            getTransitionableState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS),
            CasWebflowConstants.TRANSITION_ID_PROCEED,
            CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
    }

    private void enablePasswordManagementForFlow(final Flow flow) {
        val action = new ConsumerExecutionAction(context -> {
            WebUtils.putAccountProfileManagementEnabled(context, applicationContext.containsBean(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY));
            WebUtils.putPasswordManagementEnabled(context, casProperties.getAuthn().getPm().getCore().isEnabled());
            WebUtils.putForgotUsernameEnabled(context, casProperties.getAuthn().getPm().getForgotUsername().isEnabled());
        });
        flow.getStartActionList().add(action);
    }

    private void configurePasswordResetFlow(final Flow flow, final String id, final String viewId) {
        createFlowVariable(flow, FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);

        val propertiesToBind = Map.of(
            FLOW_VAR_ID_PASSWORD, Map.of("converter", StringToCharArrayConverter.ID),
            "confirmedPassword", Map.of("converter", StringToCharArrayConverter.ID));
        val binder = createStateBinderConfiguration(propertiesToBind);
        val viewState = createViewState(flow, id, viewId, binder);
        createStateModelBinding(viewState, FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);

        viewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE));
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
        createStateDefaultTransition(viewState, id);

        val pswChangeAction = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE));
        val transitionSet = pswChangeAction.getTransitionSet();
        transitionSet.add(
            createTransition(CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, id));
    }
}
