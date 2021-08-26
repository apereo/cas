package org.apereo.cas.adaptors.swivel.web.flow;

import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
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
 * This is {@link SwivelMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_SWIVEL_EVENT_ID = "mfa-swivel";

    public SwivelMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
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
            val flow = getFlow(registry, MFA_SWIVEL_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, SwivelTokenCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);
            
            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            setStartState(flow, initLoginFormState);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");
            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "swivel/casSwivelLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, SwivelTokenCredential.class);
            viewLoginFormState.getEntryActionList().addAll(setPrincipalAction);
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("swivelAuthenticationWebflowAction"));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_SWIVEL_EVENT_ID,
            casProperties.getAuthn().getMfa().getSwivel().getId());
    }
}
