package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccountManagementWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class AccountManagementWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Subflow id.
     */
    public static final String FLOW_ID_ACCOUNT_REGISTRATION = "acctreg";

    public AccountManagementWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final ConfigurableApplicationContext applicationContext,
                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        val startAction = new ConsumerExecutionAction(context -> AccountRegistrationUtils.putAccountRegistrationEnabled(context, true));
        flow.getStartActionList().add(startAction);

        val signUpView = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP, "acct-mgmt/casAccountSignupView");
        createTransitionForState(signUpView, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION);
        signUpView.getEntryActionList().add(createEvaluateAction("loadAccountRegistrationPropertiesAction"));

        val viewLoginForm = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        createTransitionForState(viewLoginForm, CasWebflowConstants.TRANSITION_ID_SIGNUP, signUpView.getId());

        val submitAccountRegistration = createActionState(flow, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION,
            CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT);
        createTransitionForState(submitAccountRegistration, CasWebflowConstants.TRANSITION_ID_ERROR, signUpView.getId());
        createTransitionForState(submitAccountRegistration, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO);

        createEndState(flow, CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO, "acct-mgmt/casAccountSignupViewSentInfo");

        registerAccountRegistrationFlowDefinition();
    }

    private void registerAccountRegistrationFlowDefinition() {
        val properties = casProperties.getAccountRegistration();

        val acctRegFlow = buildFlow(FLOW_ID_ACCOUNT_REGISTRATION);
        createEndState(acctRegFlow, "accountRegistrationCompleted");

        acctRegFlow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
        val completeView = createViewState(acctRegFlow, CasWebflowConstants.STATE_ID_COMPLETE_ACCOUNT_REGISTRATION, "acct-mgmt/casAccountSignupViewComplete");
        completeView.getEntryActionList().add(new ConsumerExecutionAction(context -> {
            WebUtils.putPasswordPolicyPattern(context, properties.getCore().getPasswordPolicyPattern());
            AccountRegistrationUtils.putAccountRegistrationSecurityQuestionsCount(context, properties.getCore().getSecurityQuestionsCount());
        }));

        createTransitionForState(completeView, CasWebflowConstants.TRANSITION_ID_SUBMIT, "finalizeRegistrationRequest");
        val finalize = createActionState(acctRegFlow, "finalizeRegistrationRequest", CasWebflowConstants.ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST);
        createTransitionForState(finalize, CasWebflowConstants.TRANSITION_ID_SUCCESS, "accountRegistrationCompletedView");
        createTransitionForState(finalize, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_COMPLETE_ACCOUNT_REGISTRATION);

        val completedView = createViewState(acctRegFlow, "accountRegistrationCompletedView", "acct-mgmt/casAccountSignupViewCompleted");
        createStateDefaultTransition(completedView, "accountRegistrationCompleted");

        acctRegFlow.setStartState(completeView);
        mainFlowDefinitionRegistry.registerFlowDefinition(acctRegFlow);

        val flow = getLoginFlow();
        createSubflowState(flow, CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW, FLOW_ID_ACCOUNT_REGISTRATION);

        val initializeLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        val originalTargetState = initializeLoginFormState.getTransition(CasWebflowConstants.STATE_ID_SUCCESS).getTargetStateId();
        createTransitionForState(initializeLoginFormState, CasWebflowConstants.STATE_ID_SUCCESS, "checkForAccountRegistrationToken", true);

        createDecisionState(flow, "checkForAccountRegistrationToken", "requestParameters."
            + AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN
            + " != null", CasWebflowConstants.STATE_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN, originalTargetState);

        val validateState = createActionState(flow, CasWebflowConstants.STATE_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN,
            CasWebflowConstants.ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN);
        createTransitionForState(validateState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW);
        createTransitionForState(validateState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
    }
}
