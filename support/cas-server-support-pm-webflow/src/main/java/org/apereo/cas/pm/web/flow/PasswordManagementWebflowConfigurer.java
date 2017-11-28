package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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
    public static final String FLOW_ID_PASSWORD_RESET = "pswdreset";

    /**
     * Flow id for password reset.
     */
    public static final String FLOW_VAR_ID_PASSWORD = "password";

    /**
     * Name of parameter that can be supplied to login url to force display of password change during login.
     */
    public static final String DO_CHANGE_PASSWORD_PARAMETER = "doChangePassword";

    private static final String PASSWORD_CHANGE_ACTION = "passwordChangeAction";
    private static final String SEND_PASSWORD_RESET_INSTRUCTIONS_ACTION = "sendInstructions";

    private final Action initPasswordChangeAction;

    public PasswordManagementWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                               final ApplicationContext applicationContext,
                                               final CasConfigurationProperties casProperties,
                                               final Action initPasswordChangeAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.initPasswordChangeAction = initPasswordChangeAction;
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createAccountStatusViewStates(flow);
        }
    }

    private void createAccountStatusViewStates(final Flow flow) {
        createViewState(flow, CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED, CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        createViewState(flow, CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION, CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        createViewState(flow, CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS,
                CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        createViewState(flow, CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED, CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        createViewState(flow, CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED, CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);

        if (casProperties.getAuthn().getPm().isEnabled()) {
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            configurePasswordMustChangeForAuthnWarnings(flow);
            createPasswordResetFlow();
        } else {
            createViewState(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            createViewState(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        }
    }

    private void configurePasswordMustChangeForAuthnWarnings(final Flow flow) {
        final TransitionableState warningState = getTransitionableState(flow, CasWebflowConstants.VIEW_ID_SHOW_AUTHN_WARNING_MSGS);
        warningState.getEntryActionList().add(createEvaluateAction("flowScope.pswdChangePostLogin=true"));
        createTransitionForState(warningState, "changePassword", CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
    }

    private void createPasswordResetFlow() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final boolean autoLogin = casProperties.getAuthn().getPm().isAutoLogin();

            final ViewState state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD,
                    CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO);
            final ViewState accountInfo = createViewState(flow, CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO,
                    CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(accountInfo, "findAccount", SEND_PASSWORD_RESET_INSTRUCTIONS_ACTION);
            final ActionState sendInst = createActionState(flow, SEND_PASSWORD_RESET_INSTRUCTIONS_ACTION,
                    createEvaluateAction("sendPasswordResetInstructionsAction"));
            createTransitionForState(sendInst, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(sendInst, CasWebflowConstants.TRANSITION_ID_ERROR, accountInfo.getId());
            createViewState(flow, CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO,
                    CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO);

            final Flow pswdFlow = buildFlow("classpath:/webflow/pswdreset/pswdreset-webflow.xml", FLOW_ID_PASSWORD_RESET);
            createViewState(pswdFlow, "passwordResetErrorView", CasWebflowConstants.VIEW_ID_PASSWORD_RESET_ERROR);
            createViewState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS,
                    CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);
            configurePasswordResetFlow(pswdFlow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            loginFlowDefinitionRegistry.registerFlowDefinition(pswdFlow);

            final ActionState initializeLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            final String originalTargetState = initializeLoginFormState.getTransition(CasWebflowConstants.STATE_ID_SUCCESS).getTargetStateId();
            final SubflowState pswdResetSubFlowState = createSubflowState(flow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW, FLOW_ID_PASSWORD_RESET);

            getTransitionableState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT).getEntryActionList()
                    .add(createEvaluateAction("flowScope." + DO_CHANGE_PASSWORD_PARAMETER
                            + " = requestParameters." + DO_CHANGE_PASSWORD_PARAMETER + " != null"));

            createDecisionState(flow, CasWebflowConstants.CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION,
                    "requestParameters." + SendPasswordResetInstructionsAction.PARAMETER_NAME_TOKEN + " != null",
                    CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW,
                    originalTargetState);
            createTransitionForState(initializeLoginFormState,
                    CasWebflowConstants.STATE_ID_SUCCESS,
                    CasWebflowConstants.CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION, true);
            createEndState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
            createTransitionForState(
                    getTransitionableState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS),
                    CasWebflowConstants.TRANSITION_ID_PROCEED,
                    CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
            createEndState(flow, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN, "'" + CasWebflowConfigurer.FLOW_ID_LOGIN + "'", true);

            createTransitionForState(
                    pswdResetSubFlowState,
                    CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE,
                    autoLogin ? CasWebflowConstants.STATE_ID_REAL_SUBMIT : CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

            createDecisionState(flow,
                    CasWebflowConstants.STATE_ID_CHECK_DO_CHANGE_PASSWORD,
                    "flowScope." + DO_CHANGE_PASSWORD_PARAMETER + " == true",
                    CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD,
                    getTransitionableState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT)
                            .getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId())
                    .getEntryActionList().add(createEvaluateAction("flowScope.pswdChangePostLogin=true"));

            createTransitionForState(getTransitionableState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT),
                    CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_DO_CHANGE_PASSWORD, true);

            createDecisionState(flow,
                    CasWebflowConstants.STATE_ID_PSWD_CHANGE_CHECK_POST_LOGIN,
                    "flowScope.pswdChangePostLogin == true",
                    getTransitionableState(flow, CasWebflowConstants.VIEW_ID_SHOW_AUTHN_WARNING_MSGS)
                            .getTransition(CasWebflowConstants.TRANSITION_ID_PROCEED).getTargetStateId(),
                    autoLogin ? CasWebflowConstants.STATE_ID_REAL_SUBMIT : CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

            createTransitionForState(
                    getTransitionableState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS),
                    CasWebflowConstants.TRANSITION_ID_PROCEED,
                    CasWebflowConstants.STATE_ID_PSWD_CHANGE_CHECK_POST_LOGIN);

        }
    }

    private void configurePasswordResetFlow(final Flow flow, final String id) {
        createFlowVariable(flow, FLOW_VAR_ID_PASSWORD, PasswordChangeBean.class);

        final BinderConfiguration binder = createStateBinderConfiguration(CollectionUtils.wrapList(FLOW_VAR_ID_PASSWORD, "confirmedPassword"));
        final ViewState viewState = createViewState(flow, id, id, binder);
        createStateModelBinding(viewState, FLOW_VAR_ID_PASSWORD, PasswordChangeBean.class);

        viewState.getEntryActionList().add(this.initPasswordChangeAction);
        final Transition transition = createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, PASSWORD_CHANGE_ACTION);
        transition.getAttributes().put("bind", Boolean.TRUE);
        transition.getAttributes().put("validate", Boolean.TRUE);

        createStateDefaultTransition(viewState, id);

        final ActionState pswChangeAction = createActionState(flow, PASSWORD_CHANGE_ACTION, createEvaluateAction(PASSWORD_CHANGE_ACTION));
        pswChangeAction.getTransitionSet().add(
                createTransition(PasswordChangeAction.PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS));
        pswChangeAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, id));
    }
}
