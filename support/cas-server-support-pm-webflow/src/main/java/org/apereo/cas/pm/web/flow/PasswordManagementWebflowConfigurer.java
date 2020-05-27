package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
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
    
    private final Action initPasswordChangeAction;

    public PasswordManagementWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                               final ConfigurableApplicationContext applicationContext,
                                               final CasConfigurationProperties casProperties,
                                               final Action initPasswordChangeAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.initPasswordChangeAction = initPasswordChangeAction;
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createAccountStatusViewStates(flow);
        }
    }

    private void createAccountStatusViewStates(final Flow flow) {
        flow.getStartActionList().add(requestContext -> {
            WebUtils.putPasswordManagementEnabled(requestContext, casProperties.getAuthn().getPm().isEnabled());
            return null;
        });

        createViewState(flow, CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED, CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        createViewState(flow, CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION, CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        createViewState(flow, CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS, CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        createViewState(flow, CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED, CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        createViewState(flow, CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED, CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);

        if (casProperties.getAuthn().getPm().isEnabled()) {
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            configurePasswordMustChangeForAuthnWarnings(flow);
            configurePasswordExpirationWarning(flow);
            createPasswordResetFlow();
        } else {
            createViewState(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            createViewState(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        }
    }

    private void configurePasswordExpirationWarning(final Flow flow) {
        val warningState = getTransitionableState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        warningState.getEntryActionList().add(createEvaluateAction("handlePasswordExpirationWarningMessagesAction"));
    }

    private void configurePasswordMustChangeForAuthnWarnings(final Flow flow) {
        val warningState = getTransitionableState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        warningState.getEntryActionList().add(createEvaluateAction("flowScope.pswdChangePostLogin=true"));
        createTransitionForState(warningState, "changePassword", CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
    }

    private void createPasswordResetFlow() {
        val flow = getLoginFlow();
        if (flow != null) {
            val autoLogin = casProperties.getAuthn().getPm().isAutoLogin();

            val state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_FORGOT_USERNAME, CasWebflowConstants.VIEW_ID_FORGOT_USERNAME_ACCT_INFO);

            val usernameInfo = createViewState(flow, CasWebflowConstants.VIEW_ID_FORGOT_USERNAME_ACCT_INFO, CasWebflowConstants.VIEW_ID_FORGOT_USERNAME_ACCT_INFO);
            createTransitionForState(usernameInfo, "findUsername", CasWebflowConstants.STATE_ID_FORGOT_USERNAME_INSTRUCTIONS);

            val sendUsernameInst = createActionState(flow, CasWebflowConstants.STATE_ID_FORGOT_USERNAME_INSTRUCTIONS, "sendForgotUsernameInstructionsAction");
            createTransitionForState(sendUsernameInst, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.VIEW_ID_SENT_FORGOT_USERNAME_ACCT_INFO);
            createTransitionForState(sendUsernameInst, CasWebflowConstants.TRANSITION_ID_ERROR, usernameInfo.getId());
            createViewState(flow, CasWebflowConstants.VIEW_ID_SENT_FORGOT_USERNAME_ACCT_INFO, CasWebflowConstants.VIEW_ID_SENT_FORGOT_USERNAME_ACCT_INFO);

            val viewState = createViewState(flow, CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO,
                CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(viewState, "findAccount", CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);

            val sendInst = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS, "sendPasswordResetInstructionsAction");
            createTransitionForState(sendInst, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO);
            createTransitionForState(sendInst, CasWebflowConstants.TRANSITION_ID_ERROR, viewState.getId());
            createViewState(flow, CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO, CasWebflowConstants.VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO);

            val pswdFlow = buildFlow("classpath:/webflow/pswdreset/pswdreset-webflow.xml", FLOW_ID_PASSWORD_RESET);
            pswdFlow.getStartActionList().add(requestContext -> {
                WebUtils.putPasswordManagementEnabled(requestContext, casProperties.getAuthn().getPm().isEnabled());
                return null;
            });
            createViewState(pswdFlow, "passwordResetErrorView", CasWebflowConstants.VIEW_ID_PASSWORD_RESET_ERROR);
            createViewState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);
            configurePasswordResetFlow(pswdFlow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            loginFlowDefinitionRegistry.registerFlowDefinition(pswdFlow);

            val initializeLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            val originalTargetState = initializeLoginFormState.getTransition(CasWebflowConstants.STATE_ID_SUCCESS).getTargetStateId();
            val pswdResetSubFlowState = createSubflowState(flow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW, FLOW_ID_PASSWORD_RESET);

            val createTgt = getTransitionableState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
            createTgt.getEntryActionList().add(
                createEvaluateAction(String.join(DO_CHANGE_PASSWORD_PARAMETER, "flowScope.", " = requestParameters.", " != null")));

            createDecisionState(flow, CasWebflowConstants.DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION, "requestParameters."
                + PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN
                + " != null", CasWebflowConstants.STATE_ID_PASSWORD_RESET_SUBFLOW, originalTargetState);
            createTransitionForState(initializeLoginFormState,
                CasWebflowConstants.STATE_ID_SUCCESS,
                CasWebflowConstants.DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION, true);
            createEndState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
            createTransitionForState(
                getTransitionableState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS),
                CasWebflowConstants.TRANSITION_ID_PROCEED,
                CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE);
            createEndState(flow, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN, '\'' + CasWebflowConfigurer.FLOW_ID_LOGIN + '\'', true);

            createTransitionForState(
                pswdResetSubFlowState,
                CasWebflowConstants.STATE_ID_PASSWORD_RESET_FLOW_COMPLETE,
                autoLogin ? CasWebflowConstants.STATE_ID_REAL_SUBMIT : CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

            createDecisionState(flow,
                CasWebflowConstants.STATE_ID_CHECK_DO_CHANGE_PASSWORD,
                "flowScope." + DO_CHANGE_PASSWORD_PARAMETER + " == true",
                CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD,
                createTgt
                    .getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId())
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

    private void configurePasswordResetFlow(final Flow flow, final String id) {
        createFlowVariable(flow, FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);

        val binder = createStateBinderConfiguration(CollectionUtils.wrapList(FLOW_VAR_ID_PASSWORD, "confirmedPassword"));
        val viewState = createViewState(flow, id, id, binder);
        createStateModelBinding(viewState, FLOW_VAR_ID_PASSWORD, PasswordChangeRequest.class);

        viewState.getEntryActionList().add(this.initPasswordChangeAction);
        val transition = createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION);
        transition.getAttributes().put("bind", Boolean.TRUE);
        transition.getAttributes().put("validate", Boolean.TRUE);

        createStateDefaultTransition(viewState, id);

        val pswChangeAction = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION,
            createEvaluateAction(CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_ACTION));
        val transitionSet = pswChangeAction.getTransitionSet();
        transitionSet.add(createTransition(PasswordChangeAction.PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, id));
    }
}
