package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The {@link AbstractCasMultifactorWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public abstract class AbstractCasMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String MFA_CHECK_AVAILABLE_BEAN_ID = "mfaAvailableAction";
    private static final String MFA_CHECK_BYPASS_BEAN_ID = "mfaBypassAction";
    private static final String MFA_CHECK_FAILURE_BEAN_ID = "mfaFailureAction";

    public AbstractCasMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                   final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                   final ApplicationContext applicationContext,
                                                   final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    /**
     * Register flow definition into login flow registry.
     *
     * @param sourceRegistry the source registry
     */
    protected void registerMultifactorFlowDefinitionIntoLoginFlowRegistry(final FlowDefinitionRegistry sourceRegistry) {
        val flowIds = sourceRegistry.getFlowDefinitionIds();
        for (val flowId : flowIds) {
            val definition = sourceRegistry.getFlowDefinition(flowId);
            if (definition != null) {
                LOGGER.trace("Registering flow definition [{}]", flowId);
                this.loginFlowDefinitionRegistry.registerFlowDefinition(definition);
            }
        }
    }

    private void ensureEndStateTransitionExists(final TransitionableState state,
                                                final Flow mfaProviderFlow,
                                                final String transId,
                                                final String stateId) {
        if (!containsTransition(state, transId)) {
            createTransitionForState(state, transId, stateId);
            if (!containsFlowState(mfaProviderFlow, stateId)) {
                createEndState(mfaProviderFlow, stateId);
            }
        }
    }

    /**
     * Augment mfa provider flow registry.
     *
     * @param mfaProviderFlowRegistry the mfa provider flow registry
     */
    protected void augmentMultifactorProviderFlowRegistry(final FlowDefinitionRegistry mfaProviderFlowRegistry) {
        val flowIds = mfaProviderFlowRegistry.getFlowDefinitionIds();
        Arrays.stream(flowIds).forEach(id -> {
            val flow = (Flow) mfaProviderFlowRegistry.getFlowDefinition(id);
            if (flow != null && containsFlowState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT)) {
                val states = getCandidateStatesForMultifactorAuthentication();
                states.forEach(s -> {
                    val state = getState(flow, s);
                    if (state != null) {
                        ensureEndStateTransitionExists(state, flow,
                            CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
                        ensureEndStateTransitionExists(state, flow,
                            CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS);
                        ensureEndStateTransitionExists(state, flow,
                            CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);
                        ensureEndStateTransitionExists(state, flow,
                            CasWebflowConstants.TRANSITION_ID_DENY, CasWebflowConstants.STATE_ID_MFA_DENIED);
                    } else {
                        LOGGER.error("Unable to locate state definition [{}] in flow [{}]", s, flow.getId());
                    }
                });
            }
        });

    }

    /**
     * Register multifactor provider authentication webflow.
     *
     * @param flow                    the flow
     * @param subflowId               the subflow id
     * @param mfaProviderFlowRegistry the registry
     * @param providerId              the provider id
     */
    protected void registerMultifactorProviderAuthenticationWebflow(final Flow flow,
                                                                    final String subflowId,
                                                                    final FlowDefinitionRegistry mfaProviderFlowRegistry,
                                                                    final String providerId) {
        if (!mfaProviderFlowRegistry.containsFlowDefinition(subflowId)) {
            LOGGER.error("Could not locate flow id [{}]", subflowId);
            return;
        }

        if (flow == null) {
            LOGGER.error("Unable to locate parent flow definition to register provider [{}]", providerId);
            return;
        }

        val mfaFlow = (Flow) mfaProviderFlowRegistry.getFlowDefinition(subflowId);
        mfaFlow.getStartActionList().add(createSetAction("flowScope.".concat(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID), StringUtils.quote(providerId)));

        val initStartState = (ActionState) mfaFlow.getStartState();
        val transition = (Transition) initStartState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val targetStateId = transition.getTargetStateId();
        transition.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS));

        registerMultifactorProviderBypassAction(mfaFlow);
        registerMultifactorProviderAvailableAction(mfaFlow, targetStateId);
        registerMultifactorProviderFailureAction(flow, mfaFlow);

        val subflowState = createSubflowState(flow, subflowId, subflowId);
        val states = getCandidateStatesForMultifactorAuthentication();
        LOGGER.trace("Candidate states for multifactor authentication are [{}]", states);

        states.forEach(s -> {
            LOGGER.trace("Locating state [{}] to process for multifactor authentication", s);
            val actionState = getState(flow, s);

            if (actionState == null) {
                LOGGER.error("Unable to locate state definition [{}] in flow [{}]", s, flow.getId());
            } else {
                LOGGER.trace("Adding transition [{}] to [{}] for [{}]", CasWebflowConstants.TRANSITION_ID_DENY, CasWebflowConstants.STATE_ID_MFA_DENIED, s);
                createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_DENY, CasWebflowConstants.STATE_ID_MFA_DENIED);

                LOGGER.trace("Adding transition [{}] to [{}] for [{}]",
                    CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, s);
                createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);

                LOGGER.trace("Locating transition id [{}] to process multifactor authentication for state [{}]", CasWebflowConstants.TRANSITION_ID_SUCCESS, s);
                val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

                LOGGER.trace("Locating transition id [{}] to process multifactor authentication for state [{}]",
                    CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, s);
                val targetWarningsId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS).getTargetStateId();

                LOGGER.trace("Locating transition id [{}] to process multifactor authentication for state [{}]", CasWebflowConstants.TRANSITION_ID_DENY, s);
                val targetDenied = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_DENY).getTargetStateId();

                LOGGER.trace("Location transition id [{}] to process multifactor authentication for stat [{}]",
                    CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, s);
                val targetUnavailable = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE).getTargetStateId();

                val mappings = new ArrayList<DefaultMapping>();
                val inputMapper = createMapperToSubflowState(mappings);
                val subflowMapper = createSubflowAttributeMapper(inputMapper, null);
                subflowState.setAttributeMapper(subflowMapper);

                LOGGER.trace("Creating transitions to subflow state [{}]", subflowState.getId());
                val transitionSet = subflowState.getTransitionSet();
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, targetWarningsId));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_DENY, targetDenied));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, targetUnavailable));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_CANCEL, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));

                LOGGER.trace("Creating transition [{}] for state [{}]", subflowId, actionState.getId());
                createTransitionForState(actionState, subflowId, subflowId);
            }
        });

        registerMultifactorFlowDefinitionIntoLoginFlowRegistry(mfaProviderFlowRegistry);
        augmentMultifactorProviderFlowRegistry(mfaProviderFlowRegistry);

        LOGGER.trace("Registering the [{}] flow into the flow [{}]", subflowId, flow.getId());
        val startState = flow.getTransitionableState(flow.getStartState().getId());
        createTransitionForState(startState, subflowId, subflowId, true);

        val initState = flow.getTransitionableState(CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        createTransitionForState(initState, subflowId, subflowId, true);
    }

    private void registerMultifactorProviderFailureAction(final Flow flow, final Flow mfaFlow) {
        if (flow != null) {
            val failureAction = createActionState(mfaFlow, CasWebflowConstants.TRANSITION_ID_MFA_FAILURE, createEvaluateAction(MFA_CHECK_FAILURE_BEAN_ID));
            createTransitionForState(failureAction, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
            createTransitionForState(failureAction, CasWebflowConstants.TRANSITION_ID_BYPASS, CasWebflowConstants.TRANSITION_ID_SUCCESS);

            LOGGER.trace("Adding end state [{}] with transition to [{}] to flow [{}] for MFA",
                CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, CasWebflowConstants.VIEW_ID_MFA_UNAVAILABLE, flow.getId());
            createEndState(flow, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, CasWebflowConstants.VIEW_ID_MFA_UNAVAILABLE);

            LOGGER.trace("Adding end state [{}] with transition to [{}] to flow [{}] for MFA",
                CasWebflowConstants.STATE_ID_MFA_DENIED, CasWebflowConstants.VIEW_ID_MFA_DENIED, flow.getId());
            createEndState(flow, CasWebflowConstants.STATE_ID_MFA_DENIED, CasWebflowConstants.VIEW_ID_MFA_DENIED);
        }
    }

    private void registerMultifactorProviderAvailableAction(final Flow mfaFlow, final String targetStateId) {
        val availableAction = createActionState(mfaFlow, CasWebflowConstants.STATE_ID_MFA_CHECK_AVAILABLE, createEvaluateAction(MFA_CHECK_AVAILABLE_BEAN_ID));
        if (mfaFlow.containsState(CasWebflowConstants.STATE_ID_MFA_PRE_AUTH)) {
            createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.STATE_ID_MFA_PRE_AUTH);
        } else {
            createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_YES, targetStateId);
        }
        createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_NO, CasWebflowConstants.TRANSITION_ID_MFA_FAILURE);
    }

    private void registerMultifactorProviderBypassAction(final Flow mfaFlow) {
        val bypassAction = createActionState(mfaFlow, CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS,
            createEvaluateAction(MFA_CHECK_BYPASS_BEAN_ID));
        createTransitionForState(bypassAction, CasWebflowConstants.TRANSITION_ID_NO, CasWebflowConstants.STATE_ID_MFA_CHECK_AVAILABLE);
        createTransitionForState(bypassAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    protected Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return CollectionUtils.wrapSet(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }
}
