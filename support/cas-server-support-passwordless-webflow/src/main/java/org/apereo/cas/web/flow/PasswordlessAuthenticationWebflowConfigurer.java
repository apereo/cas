package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.Arrays;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PasswordlessAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Transition to obtain username.
     */
    static final String TRANSITION_ID_PASSWORDLESS_GET_USERID = "passwordlessGetUserId";

    /**
     * State to display username.
     */
    static final String STATE_ID_PASSWORDLESS_DISPLAY = "passwordlessDisplayUser";

    /**
     * State to determine mfa for passwordless.
     */
    static final String STATE_ID_PASSWORDLESS_DETERMINE_MFA = "determineMultifactorPasswordlessAuthentication";

    /**
     * State to determine delegated authn.
     */
    static final String STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN= "determineDelegatedAuthentication";

    /**
     * State to verify account.
     */
    static final String STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT = "passwordlessVerifyAccount";

    /**
     * State to accept authentication.
     */
    static final String STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION = "acceptPasswordlessAuthentication";

    /**
     * State to get user id.
     */
    static final String STATE_ID_PASSWORDLESS_GET_USERID = "passwordlessGetUserIdView";

    public PasswordlessAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                       final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                       final ConfigurableApplicationContext applicationContext,
                                                       final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createStateInitialPasswordless(flow);
            createStateGetUserIdentifier(flow);
            createStateVerifyPasswordlessAccount(flow);
            createStateDisplayPasswordless(flow);
            createStateDetermineDelegatedAuthenticationAction(flow);
            createStateDetermineMultifactorAuthenticationAction(flow);
            createStateAcceptPasswordless(flow);
        }
    }

    /**
     * Create state initial passwordless.
     *
     * @param flow the flow
     */
    protected void createStateInitialPasswordless(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        createTransitionForState(state, TRANSITION_ID_PASSWORDLESS_GET_USERID, STATE_ID_PASSWORDLESS_GET_USERID, true);
    }

    /**
     * Create state accept passwordless.
     *
     * @param flow the flow
     */
    protected void createStateAcceptPasswordless(final Flow flow) {
        val acceptAction = createEvaluateAction("acceptPasswordlessAuthenticationAction");
        val acceptState = createActionState(flow, STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION, acceptAction);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, STATE_ID_PASSWORDLESS_DISPLAY);

        val submission = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val transition = (Transition) submission.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val targetStateId = transition.getTargetStateId();
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetStateId);
    }

    /**
     * Create state display passwordless.
     *
     * @param flow the flow
     */
    protected void createStateDisplayPasswordless(final Flow flow) {
        val viewStateDisplay = createViewState(flow, STATE_ID_PASSWORDLESS_DISPLAY, "passwordless/casPasswordlessDisplayView");
        viewStateDisplay.getEntryActionList().add(createEvaluateAction("displayBeforePasswordlessAuthenticationAction"));
        createTransitionForState(viewStateDisplay, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION);
    }

    /**
     * Create state verify passwordless account.
     *
     * @param flow the flow
     */
    protected void createStateVerifyPasswordlessAccount(final Flow flow) {
        val verifyAccountState = createActionState(flow, STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT, "verifyPasswordlessAccountAuthenticationAction");
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ID_PASSWORDLESS_GET_USERID);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN);

        val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        val transition = state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_PROMPT, transition.getTargetStateId());
    }

    /**
     * Create state determine delegated authentication action.
     *
     * @param flow the flow
     */
    protected void createStateDetermineDelegatedAuthenticationAction(final Flow flow) {
        val verifyAccountState = createActionState(flow, STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN, "determineDelegatedAuthenticationAction");
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ID_PASSWORDLESS_GET_USERID);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_PASSWORDLESS_DETERMINE_MFA);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_REDIRECT, "redirectToDelegatedIdentityProviderView");
        createEndState(flow, "redirectToDelegatedIdentityProviderView", "flashScope.delegatedClientIdentityProvider.redirectUrl", true);
    }

    /**
     * Create state determine multifactor authentication action.
     *
     * @param flow the flow
     */
    protected void createStateDetermineMultifactorAuthenticationAction(final Flow flow) {
        val verifyAccountState = createActionState(flow, STATE_ID_PASSWORDLESS_DETERMINE_MFA, "determineMultifactorPasswordlessAuthenticationAction");
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_PASSWORDLESS_DISPLAY);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ID_PASSWORDLESS_GET_USERID);

        val cfgs = applicationContext.getBeansOfType(CasMultifactorWebflowConfigurer.class).values();
        cfgs.forEach(cfg -> cfg.getMultifactorAuthenticationFlowDefinitionRegistries()
            .forEach(registry -> Arrays.stream(registry.getFlowDefinitionIds())
                .forEach(id -> createTransitionForState(verifyAccountState, id, id))));
    }

    /**
     * Create state get user identifier.
     *
     * @param flow the flow
     */
    protected void createStateGetUserIdentifier(final Flow flow) {
        val viewState = createViewState(flow, STATE_ID_PASSWORDLESS_GET_USERID, "passwordless/casPasswordlessGetUserIdView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT);
    }
}
