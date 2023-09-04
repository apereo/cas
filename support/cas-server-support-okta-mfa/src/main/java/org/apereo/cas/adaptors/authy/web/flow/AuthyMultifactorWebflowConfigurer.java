package org.apereo.cas.adaptors.authy.web.flow;

import lombok.val;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaCode;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaEmailAddress;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaMfaSelect;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaPhoneNumber;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

import static org.apereo.cas.adaptors.authy.core.Constants.*;

/**
 * This is {@link AuthyMultifactorWebflowConfigurer}.
 *
 * @author Jérémie POISSON
 */
public class AuthyMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_AUTHY_EVENT_ID = "mfa-authy";

    public AuthyMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                             final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                             final FlowDefinitionRegistry flowDefinitionRegistry,
                                             final ConfigurableApplicationContext applicationContext,
                                             final CasConfigurationProperties casProperties,
                                             final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext,
                casProperties, Optional.of(flowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {

            val flow = getFlow(registry, MFA_AUTHY_EVENT_ID);
            //create flow variable for the phone number and the code the user will enter
            createFlowVariable(flow, "number", OktaPhoneNumber.class);
            createFlowVariable(flow, "email", OktaEmailAddress.class);
            createFlowVariable(flow, "code", OktaCode.class);
            createFlowVariable(flow, "identificationType", OktaMfaSelect.class);

            //initial action to the flow to set up the initial flow state
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

            // Create the end state for the flow
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            // Create the action state to initialize the login form
            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                    createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));

            // Create transitions for the initLoginFormState
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, ACTION_USER_CHECK);
            val returnToMfaSelectionTransition = createTransition(TRANSITION_SELECT_MFA, STATE_SELECT_MFA);


            // Create the Check user Action State
            val userCheckActionState = createActionState(flow, ACTION_USER_CHECK,
                    createEvaluateAction(ACTION_USER_CHECK));
            createTransitionForState(userCheckActionState, CHALLENGE, STATE_CHALLENGE); //if the user is enrolled, go to verify factor state
            createTransitionForState(userCheckActionState, CHALLENGE_PUSH, STATE_CHALLENGE_PUSH); //if the user selects email, go to the email address entry state
            createTransitionForState(userCheckActionState, SELECT_MFA, STATE_SELECT_MFA); //if the user is not enrolled, go to the mfa selection state
            createTransitionForState(userCheckActionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM); //if there is an error, go back to the initial login form

            // Create the MFA Selection View State
            val selectMfaBinder = createStateBinderConfiguration(CollectionUtils.wrapList("identificationMethod"));
            val selectMfaState = createViewState(flow, STATE_SELECT_MFA, VIEW_SELECT_MFA, selectMfaBinder);
            createStateModelBinding(selectMfaState, "identificationType", OktaMfaSelect.class);
            val setPrincipalActionForMfa = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            selectMfaState.getEntryActionList().addAll(setPrincipalActionForMfa);
            createTransitionForState(selectMfaState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_SELECT_MFA);

            // Create the MFA Selection Action State
            val selectTypeActionState = createActionState(flow, ACTION_SELECT_MFA, createEvaluateAction(ACTION_SELECT_MFA));
            createTransitionForState(selectTypeActionState, ENROLL_SMS, STATE_ENROLL_PHONE); //if the user selects sms, go to the phone number entry state
            createTransitionForState(selectTypeActionState, ENROLL_EMAIL, STATE_ENROLL_EMAIL); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, ENROLL_OKTA_PUSH, STATE_ENROLL_OKTA); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, ENROLL_OKTA_TOTP, STATE_ENROLL_OKTA); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, ENROLL_GOOGLE, STATE_ENROLL_OKTA); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, CHALLENGE, STATE_CHALLENGE); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, CHALLENGE_PUSH, STATE_CHALLENGE_PUSH); //if the user selects email, go to the email address entry state
            createTransitionForState(selectTypeActionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM); //if there is an error, go back to the initial login form

            // Create the Phone number Entry View State
            val phoneNumberBinder = createStateBinderConfiguration(CollectionUtils.wrapList("phoneNumber"));
            val phoneNumberEntryState = createViewState(flow, STATE_ENROLL_PHONE, VIEW_ENROLL_PHONE, phoneNumberBinder);
            createStateModelBinding(phoneNumberEntryState, "number", OktaPhoneNumber.class);
            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            phoneNumberEntryState.getEntryActionList().addAll(setPrincipalAction);
            createTransitionForState(phoneNumberEntryState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_ENROLL_PHONE);  //if the user submits the phone number, go to the enrollment action
            createTransitionForState(phoneNumberEntryState, TRANSITION_SELECT_MFA, STATE_SELECT_MFA);

            // Create the Phone number Entry Action State
            val phoneEnrollmentActionState = createActionState(flow, ACTION_ENROLL_PHONE,
                    createEvaluateAction(ACTION_ENROLL_PHONE));
            createTransitionForState(phoneEnrollmentActionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_CHALLENGE); //when the user is enrolled, go to verify factor state
            createTransitionForState(phoneEnrollmentActionState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ENROLL_PHONE); //if there is an error, go back to the phone number entry state

            // Create the Email address Entry View State
            val emailAddressBinder = createStateBinderConfiguration(CollectionUtils.wrapList("emailAddress"));
            val emailAddressEntryState = createViewState(flow, STATE_ENROLL_EMAIL, VIEW_ENROLL_EMAIL, emailAddressBinder);
            createStateModelBinding(emailAddressEntryState, "email", OktaEmailAddress.class);
            val setPrincipalActionForEmail = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            emailAddressEntryState.getEntryActionList().addAll(setPrincipalActionForEmail);
            createTransitionForState(emailAddressEntryState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_ENROLL_EMAIL);  //if the user submits the email address, go to the enrollment action
            createTransitionForState(emailAddressEntryState, TRANSITION_SELECT_MFA, STATE_SELECT_MFA);

            // Create the Email address Entry Action State
            val emailEnrollmentActionState = createActionState(flow, ACTION_ENROLL_EMAIL,
                    createEvaluateAction(ACTION_ENROLL_EMAIL));
            createTransitionForState(emailEnrollmentActionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_CHALLENGE); //when the user is enrolled, go to verify factor state
            createTransitionForState(emailEnrollmentActionState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ENROLL_EMAIL); //if there is an error, go back to the email address entry state

            // Create the TOTP (Okta & Google) Entry View State
            val oktaVerifyEntryState = createViewState(flow, STATE_ENROLL_OKTA, VIEW_ENROLL_OKTA);
            // Define action to enroll the user in Okta Verify and generate the QR code
            val oktaVerifyEnrollmentAction = createEvaluateAction(ACTION_ENROLL_TOTP_VIEW);
            oktaVerifyEntryState.getEntryActionList().add(oktaVerifyEnrollmentAction);
            // Add principal to view scope
            val setPrincipalActionForOktaVerify = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            oktaVerifyEntryState.getEntryActionList().addAll(setPrincipalActionForOktaVerify);
            // Transition from Okta Verify entry state to evaluate enrollment action state
            createTransitionForState(oktaVerifyEntryState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_ENROLL_TOTP);
            // Transition to selectMfaState when the user clicks the "Return to MFA Selection" button
            createTransitionForState(oktaVerifyEntryState, TRANSITION_SELECT_MFA, STATE_SELECT_MFA);

            // Create action state for evaluating enrollment when the Next button is clicked
            val totpEnrollActionState = createActionState(flow, ACTION_ENROLL_TOTP,
                    createEvaluateAction(ACTION_ENROLL_TOTP));
            createTransitionForState(totpEnrollActionState, "enrollPushSuccess", CasWebflowConstants.STATE_ID_SUCCESS); // or another state if enrolled
            createTransitionForState(totpEnrollActionState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_CHALLENGE); // or another state if not enrolled
            createTransitionForState(totpEnrollActionState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ENROLL_OKTA); //if there is an error, go back to the email address entry state

            // Create Verify Factor View State
            val verifyFactorBinder = createStateBinderConfiguration(CollectionUtils.wrapList("secretCode"));
            val verifyFactorState = createViewState(flow, STATE_CHALLENGE, VIEW_CHALLENGE, verifyFactorBinder);
            createStateModelBinding(verifyFactorState, "code", OktaCode.class);
            val setPrincipalActionForCode = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            verifyFactorState.getEntryActionList().addAll(setPrincipalActionForCode);
            createTransitionForState(verifyFactorState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_CHALLENGE);//if the user submits the code, go to verify factor action
            createTransitionForState(verifyFactorState, "resend", ACTION_RESEND_CODE);
            // Add "returnToMfaSelection" transition to the verifyFactorState
            createTransitionForState(verifyFactorState, TRANSITION_SELECT_MFA, STATE_SELECT_MFA);

            // Create Verify Factor Action State
            val verifyFactorActionState = createActionState(flow, ACTION_CHALLENGE,
                    createEvaluateAction(ACTION_CHALLENGE));
            createTransitionForState(verifyFactorActionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS); //if the code is correct, the user is logged
            createTransitionForState(verifyFactorActionState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_CHALLENGE); //if the code is incorrect, go back to verify factor state

            // Create Push Validation View State
            val verifyPushFactorState = createViewState(flow, STATE_CHALLENGE_PUSH, VIEW_CHALLENGE_PUSH);  // "verifyPushFactor" should be the name of your new template
            val setPrincipalActionForPush = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            verifyPushFactorState.getEntryActionList().addAll(setPrincipalActionForPush);
            createTransitionForState(verifyPushFactorState, CasWebflowConstants.TRANSITION_ID_SUBMIT, ACTION_CHALLENGE_PUSH);  // transition to validation action when the button is clicked
            createTransitionForState(verifyPushFactorState, TRANSITION_SELECT_MFA, STATE_SELECT_MFA);

            // Create Push Validation Action State
            val pushValidationActionState = createActionState(flow, ACTION_CHALLENGE_PUSH, createEvaluateAction(ACTION_CHALLENGE_PUSH));
            createTransitionForState(pushValidationActionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(pushValidationActionState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_CHALLENGE_PUSH);

            val resendCodeActionState = createActionState(flow, ACTION_RESEND_CODE,
                    createEvaluateAction(ACTION_RESEND_CODE));
            createTransitionForState(resendCodeActionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_CHALLENGE);
            createTransitionForState(selectTypeActionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM); //if there is an error, go back to the initial login form

            setStartState(flow, initLoginFormState);

            registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_AUTHY_EVENT_ID,
                    casProperties.getAuthn().getMfa().getAuthy().getId());
        });
    }
}
