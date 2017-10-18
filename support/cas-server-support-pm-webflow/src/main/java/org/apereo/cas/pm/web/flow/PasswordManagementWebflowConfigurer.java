package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
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
        createEndState(flow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS, CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);

        if (casProperties.getAuthn().getPm().isEnabled()) {
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            configurePasswordResetFlow(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            createPasswordResetFlow();
        } else {
            createViewState(flow, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD, CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
            createViewState(flow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        }
    }

    private void createPasswordResetFlow() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
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
            createEndState(pswdFlow, CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS,
                    CasWebflowConstants.VIEW_ID_PASSWORD_UPDATE_SUCCESS);
            configurePasswordResetFlow(pswdFlow, CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
            loginFlowDefinitionRegistry.registerFlowDefinition(pswdFlow);
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
