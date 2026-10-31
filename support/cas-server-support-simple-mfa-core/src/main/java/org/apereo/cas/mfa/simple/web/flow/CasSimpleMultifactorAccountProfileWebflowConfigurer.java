package org.apereo.cas.mfa.simple.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.PopulateMessageContextAction;
import org.apereo.cas.web.flow.configurer.acct.AccountProfileContributionWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasSimpleMultifactorAccountProfileWebflowConfigurer}.
 * Shows the contact methods that receive one-time codes in the account profile, and, when email registration
 * is turned on, lets the user register an email address with the same verify-and-confirm steps as the login flow.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasSimpleMultifactorAccountProfileWebflowConfigurer extends AccountProfileContributionWebflowConfigurer {
    /**
     * Transition from the account profile to the email registration view.
     */
    public static final String TRANSITION_ID_REGISTER_EMAIL = "registerContactEmail";

    private static final String STATE_ID_REGISTRATION_VIEW = "simpleMfaAccountProfileRegistrationView";

    private static final String STATE_ID_VERIFY_EMAIL = "simpleMfaAccountProfileVerifyEmail";

    private static final String STATE_ID_UPDATE_EMAIL_VIEW = "simpleMfaAccountProfileUpdateEmailView";

    private static final String STATE_ID_UPDATE_EMAIL = "simpleMfaAccountProfileUpdateEmail";

    private static final String STATE_ID_REGISTRATION_FINALIZED = "simpleMfaAccountProfileRegistrationFinalized";

    public CasSimpleMultifactorAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                               final FlowDefinitionRegistry flowDefinitionRegistry,
                                                               final ConfigurableApplicationContext applicationContext,
                                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        withRenderAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_CONTACT_METHODS);
    }

    @Override
    protected void customizeAccountFlow(final Flow accountFlow, final ViewState accountView) {
        if (casProperties.getAuthn().getMfa().getSimple().getMail().isRegistrationEnabled()) {
            val registrationView = createViewState(accountFlow, STATE_ID_REGISTRATION_VIEW, "simple-mfa/casSimpleMfaRegisterRecipientsView");
            registrationView.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_REGISTRATION));
            createTransitionForState(registrationView, CasWebflowConstants.TRANSITION_ID_REGISTER, STATE_ID_VERIFY_EMAIL);

            val verifyEmailState = createActionState(accountFlow, STATE_ID_VERIFY_EMAIL, CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VERIFY_EMAIL);
            verifyEmailState.getEntryActionList().add(ConsumerExecutionAction.EVENT_ATTRIBUTES_TO_FLOW_SCOPE);
            createTransitionForState(verifyEmailState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_UPDATE_EMAIL_VIEW);
            createTransitionForState(verifyEmailState, CasWebflowConstants.TRANSITION_ID_ERROR, registrationView.getId());

            val updateEmailView = createViewState(accountFlow, STATE_ID_UPDATE_EMAIL_VIEW, "simple-mfa/casSimpleMfaUpdateEmailRegistrationView");
            createTransitionForState(updateEmailView, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_UPDATE_EMAIL);

            val updateEmailState = createActionState(accountFlow, STATE_ID_UPDATE_EMAIL, CasWebflowConstants.ACTION_ID_MFA_SIMPLE_UPDATE_EMAIL);
            createTransitionForState(updateEmailState, CasWebflowConstants.TRANSITION_ID_RESUME, STATE_ID_REGISTRATION_FINALIZED);
            createTransitionForState(updateEmailState, CasWebflowConstants.TRANSITION_ID_ERROR, updateEmailView.getId());

            val finalizeAction = new PopulateMessageContextAction.Info("screen.account.contact.email.registered")
                .setEventId(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val finalizeState = createActionState(accountFlow, STATE_ID_REGISTRATION_FINALIZED, finalizeAction);
            createStateDefaultTransition(finalizeState, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);

            createTransitionForState(accountView, TRANSITION_ID_REGISTER_EMAIL, registrationView.getId());
        }
    }
}
