package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasSimpleMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CasSimpleMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    public CasSimpleMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                 final FlowDefinitionRegistry flowDefinitionRegistry,
                                                 final FlowDefinitionRegistry mfaFlowDefinitionRegistry,
                                                 final ConfigurableApplicationContext applicationContext,
                                                 final CasConfigurationProperties casProperties,
                                                 final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext,
            casProperties, Optional.of(mfaFlowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        val providerId = casProperties.getAuthn().getMfa().getSimple().getId();
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, providerId);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, CasSimpleMultifactorTokenCredential.class);
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_SIMPLE_MFA_SEND_TOKEN);
            setStartState(flow, initLoginFormState);
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);
            createEndState(flow, CasWebflowConstants.STATE_ID_UNAVAILABLE);

            val sendSimpleToken = createActionState(flow, CasWebflowConstants.STATE_ID_SIMPLE_MFA_SEND_TOKEN, CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN);
            createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_UNAVAILABLE);
            createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_SELECT, "selectRecipientsView");

            val registrationEnabled = casProperties.getAuthn().getMfa().getSimple().getMail().isRegistrationEnabled();
            if (registrationEnabled) {
                createTransitionForState(sendSimpleToken, CasWebflowConstants.TRANSITION_ID_REGISTER, "recipientsRegistrationView");
            }

            val selectRecipientsView = createViewState(flow, "selectRecipientsView", "simple-mfa/casSimpleMfaSelectRecipientsView");
            createTransitionForState(selectRecipientsView, CasWebflowConstants.TRANSITION_ID_SELECT, CasWebflowConstants.STATE_ID_SIMPLE_MFA_SEND_TOKEN);

            if (registrationEnabled) {
                createEmailRegistrationStates(flow);
            }
            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            val propertiesToBind = CollectionUtils.wrapList("token");
            val binder = createStateBinderConfiguration(propertiesToBind);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                "simple-mfa/casSimpleMfaLoginView", binder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, CasSimpleMultifactorTokenCredential.class);
            viewLoginFormState.getEntryActionList().add(setPrincipalAction);

            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, createTransitionAttributes(true, true));

            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_RESEND,
                CasWebflowConstants.STATE_ID_SIMPLE_MFA_SEND_TOKEN,
                createTransitionAttributes(false, false));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        });
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), providerId);
    }

    private void createEmailRegistrationStates(final Flow flow) {
        val registrationView = createViewState(flow, "recipientsRegistrationView", "simple-mfa/casSimpleMfaRegisterRecipientsView");
        registrationView.getEntryActionList().add(ConsumerExecutionAction.EVENT_ATTRIBUTES_TO_FLOW_SCOPE);
        createTransitionForState(registrationView, CasWebflowConstants.TRANSITION_ID_REGISTER, CasWebflowConstants.STATE_ID_SIMPLE_MFA_VERIFY_EMAIL);
        val verifyEmailState = createActionState(flow, CasWebflowConstants.STATE_ID_SIMPLE_MFA_VERIFY_EMAIL, CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VERIFY_EMAIL);
        verifyEmailState.getEntryActionList().add(ConsumerExecutionAction.EVENT_ATTRIBUTES_TO_FLOW_SCOPE);
        createTransitionForState(verifyEmailState, CasWebflowConstants.TRANSITION_ID_ERROR, registrationView.getId());
        createTransitionForState(verifyEmailState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "updateEmailRegistrationView");

        val updateEmailViewState = createViewState(flow, "updateEmailRegistrationView", "simple-mfa/casSimpleMfaUpdateEmailRegistrationView");
        createTransitionForState(updateEmailViewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "updateEmailRegistration");
        createTransitionForState(updateEmailViewState, CasWebflowConstants.TRANSITION_ID_ERROR, updateEmailViewState.getId());

        val updateEmailRegistrationState = createActionState(flow, "updateEmailRegistration", CasWebflowConstants.ACTION_ID_MFA_SIMPLE_UPDATE_EMAIL);
        createTransitionForState(updateEmailRegistrationState, CasWebflowConstants.TRANSITION_ID_RESUME, CasWebflowConstants.STATE_ID_SIMPLE_MFA_SEND_TOKEN);
        createTransitionForState(updateEmailRegistrationState, CasWebflowConstants.TRANSITION_ID_ERROR, updateEmailViewState.getId());
    }
}
