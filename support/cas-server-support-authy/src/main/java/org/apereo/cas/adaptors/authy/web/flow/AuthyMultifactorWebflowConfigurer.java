package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.AuthyTokenCredential;
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
 * This is {@link AuthyMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
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
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, AuthyTokenCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "registerUserAction");
            setStartState(flow, initLoginFormState);

            val registerState = createActionState(flow, "registerUserAction",
                createEvaluateAction("authyAuthenticationRegistrationWebflowAction"));
            createStateDefaultTransition(registerState, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("authyAuthenticationWebflowAction"));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "authy/casAuthyLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, AuthyTokenCredential.class);
            viewLoginFormState.getEntryActionList().addAll(setPrincipalAction);
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));
        });
        
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_AUTHY_EVENT_ID,
                casProperties.getAuthn().getMfa().getAuthy().getId());
    }
}
