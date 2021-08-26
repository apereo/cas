package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.U2FTokenCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link U2FMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_U2F_EVENT_ID = "mfa-u2f";

    public U2FMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
            val flow = getFlow(registry, MFA_U2F_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, U2FTokenCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            setStartState(flow, initLoginFormState);

            val checkState = createActionState(flow, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION,
                createEvaluateAction("u2fCheckAccountRegistrationAction"));
            createTransitionForState(checkState, CasWebflowConstants.TRANSITION_ID_REGISTER, "viewRegistrationU2f");
            createTransitionForState(checkState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val saveState = createActionState(flow, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION,
                createEvaluateAction("u2fSaveAccountRegistrationAction"));
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("u2fAuthenticationWebflowAction"));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val viewRegState = createViewState(flow, "viewRegistrationU2f", "u2f/casU2fRegistrationView");
            viewRegState.getEntryActionList()
                .addAll(createEvaluateAction("u2fStartRegistrationAction"), setPrincipalAction);
            createTransitionForState(viewRegState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION);

            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM,
                "u2f/casU2fLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, U2FTokenCredential.class);
            viewLoginFormState.getEntryActionList()
                .addAll(createEvaluateAction("u2fStartAuthenticationAction"), setPrincipalAction);

            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_U2F_EVENT_ID,
            casProperties.getAuthn().getMfa().getU2f().getId());
    }
}
