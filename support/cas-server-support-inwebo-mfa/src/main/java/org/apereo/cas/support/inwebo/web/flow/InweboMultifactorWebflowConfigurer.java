package org.apereo.cas.support.inwebo.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.web.flow.actions.WebflowConstants;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

/**
 * The Inwebo webflow configurer.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public class InweboMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_INWEBO_EVENT_ID = "mfa-inwebo";

    public InweboMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                              final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final FlowDefinitionRegistry flowDefinitionRegistry,
                                              final ConfigurableApplicationContext applicationContext,
                                              final CasConfigurationProperties casProperties,
                                              final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, Optional.of(flowDefinitionRegistry),
            mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, MFA_INWEBO_EVENT_ID);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initializeLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, "inweboSuccessAction");
            createTransitionForState(initializeLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "checkUser");
            setStartState(flow, initializeLoginFormState);

            val checkUserState = createActionState(flow, "checkUser", "inweboCheckUserAction");
            createTransitionForState(checkUserState, WebflowConstants.PUSH, "startPush");
            createTransitionForState(checkUserState, WebflowConstants.BROWSER, "startBrowser");
            createTransitionForState(checkUserState, WebflowConstants.SELECT, "selectAuthent");
            createTransitionForState(checkUserState, CasWebflowConstants.TRANSITION_ID_ERROR, "inweboError");

            val selectAuthentState = createViewState(flow, "selectAuthent", "inwebo/casInweboSelectAuthnView");
            createTransitionForState(selectAuthentState, WebflowConstants.PUSH, "startPush");
            createTransitionForState(selectAuthentState, WebflowConstants.BROWSER, "startBrowser");

            val startBrowserState = createViewState(flow, "startBrowser", "inwebo/casInweboBrowserAuthnView");
            createTransitionForState(startBrowserState, "enroll", "mustEnroll");
            createTransitionForState(startBrowserState, "otp", CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            createTransitionForState(startBrowserState, CasWebflowConstants.TRANSITION_ID_ERROR, "inweboError");

            val mustEnrollState = createActionState(flow, "mustEnroll", "inweboMustEnrollAction");
            createTransitionForState(mustEnrollState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "inweboError");

            val startPushState = createActionState(flow, "startPush", "inweboPushAuthenticateAction");
            createTransitionForState(startPushState, CasWebflowConstants.TRANSITION_ID_SUCCESS, "pendingCheckResult");
            createTransitionForState(startPushState, CasWebflowConstants.TRANSITION_ID_ERROR, "inweboError");

            val pendingCheckResultState = createViewState(flow, "pendingCheckResult", "inwebo/casInweboCheckResultView");
            createTransitionForState(pendingCheckResultState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_REAL_SUBMIT);

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, "inweboCheckAuthenticationAction");
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, WebflowConstants.PENDING, "pendingCheckResult");
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, "inweboError");

            val inweboErrorState = createViewState(flow, "inweboError", "inwebo/casInweboErrorView");
            createTransitionForState(inweboErrorState, WebflowConstants.BROWSER, "startBrowser");
            createTransitionForState(inweboErrorState, CasWebflowConstants.TRANSITION_ID_RETRY, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
            createTransitionForState(inweboErrorState, CasWebflowConstants.TRANSITION_ID_ERROR, "inweboError");
        });

        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_INWEBO_EVENT_ID,
            casProperties.getAuthn().getMfa().getInwebo().getId());
    }
}
