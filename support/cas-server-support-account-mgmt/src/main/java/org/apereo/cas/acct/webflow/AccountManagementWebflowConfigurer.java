package org.apereo.cas.acct.webflow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccountManagementWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class AccountManagementWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public AccountManagementWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final ConfigurableApplicationContext applicationContext,
                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        val startAction = new ConsumerExecutionAction(context -> WebUtils.putAccountManagementSignUpEnabled(context, true));
        flow.getStartActionList().add(startAction);

        val signUpView = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP, "acct-mgmt/casAccountSignupView");
        createTransitionForState(signUpView, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION);
        signUpView.getEntryActionList().add(createEvaluateAction("loadAccountRegistrationPropertiesAction"));

        val viewLoginForm = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        createTransitionForState(viewLoginForm, CasWebflowConstants.TRANSITION_ID_SIGNUP, signUpView.getId());

        val submitAccountRegistration = createActionState(flow, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION, "submitAccountRegistrationAction");
        createTransitionForState(submitAccountRegistration, CasWebflowConstants.TRANSITION_ID_ERROR, signUpView.getId());
        createTransitionForState(submitAccountRegistration, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO);

        createEndState(flow, CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO, "acct-mgmt/casAccountSignupViewSentInfo");

    }
}
