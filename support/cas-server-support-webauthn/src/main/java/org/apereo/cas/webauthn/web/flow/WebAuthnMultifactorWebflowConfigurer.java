package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.webauthn.WebAuthnCredential;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class WebAuthnMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {
    /**
     * Webflow event id.
     */
    public static final String MFA_WEB_AUTHN_EVENT_ID = "mfa-webauthn";

    public WebAuthnMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
            val flow = getFlow(registry, MFA_WEB_AUTHN_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "accountRegistrationCheck");
            setStartState(flow, initLoginFormState);

            val acctRegCheckState = createActionState(flow, "accountRegistrationCheck",
                createEvaluateAction("webAuthnCheckAccountRegistrationAction"));
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_REGISTER, "viewRegistrationWebAuthn");
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val viewRegState = createViewState(flow, "viewRegistrationWebAuthn", "casWebAuthnRegistrationView");
            viewRegState.getEntryActionList().addAll(createEvaluateAction("webAuthnStartRegistrationAction"), setPrincipalAction);
            createTransitionForState(viewRegState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "saveRegistration");

            val saveState = createActionState(flow, "saveRegistration", createEvaluateAction("webAuthnSaveAccountRegistrationAction"));
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "accountRegistrationCheck");
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "casWebAuthnLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);
            viewLoginFormState.getEntryActionList().addAll(
                createEvaluateAction("webAuthnStartAuthenticationAction"), setPrincipalAction);
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("webAuthnAuthenticationWebflowAction"));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_ERROR);
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(),
            MFA_WEB_AUTHN_EVENT_ID,
            casProperties.getAuthn().getMfa().getWebAuthn().getId());
    }
}
