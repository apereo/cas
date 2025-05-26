package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorTokenCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasTwilioMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class CasTwilioMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    public CasTwilioMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
        val providerId = casProperties.getAuthn().getMfa().getTwilio().getId();
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, providerId);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, CasTwilioMultifactorTokenCredential.class);
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_MFA_TWILIO_SEND_TOKEN);
            setStartState(flow, initLoginFormState);
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);
            createEndState(flow, CasWebflowConstants.STATE_ID_UNAVAILABLE);

            val sendTwilioToken = createActionState(flow, CasWebflowConstants.STATE_ID_MFA_TWILIO_SEND_TOKEN, CasWebflowConstants.ACTION_ID_MFA_TWILIO_SEND_TOKEN);
            createTransitionForState(sendTwilioToken, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_UNAVAILABLE);
            createTransitionForState(sendTwilioToken, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            val propertiesToBind = CollectionUtils.wrapList("token");
            val binder = createStateBinderConfiguration(propertiesToBind);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "twilio-mfa/casTwilioMfaLoginView", binder);

            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, CasTwilioMultifactorTokenCredential.class);
            viewLoginFormState.getEntryActionList().add(setPrincipalAction);

            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, createTransitionAttributes(true, true));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION));
            realSubmitState.getEntryActionList().add(new SetCredentialMetadataAuthenticationAction());
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        });
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), providerId);
    }
}

