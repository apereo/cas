package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.NoMatchingTransitionException;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link AbstractCasMultifactorWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Getter
public abstract class AbstractCasMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer
    implements CasMultifactorWebflowConfigurer {

    private static final Set<String> SCOPES = Set.of("flowScope", "conversationScope");

    private static final String LOG_MESSAGE_TRANSITION_ID = "Locating transition id [{}] to process multifactor authentication for state [{}]...";

    /**
     * The flow definition registry for the mfa flow.
     */
    protected final List<FlowDefinitionRegistry> multifactorAuthenticationFlowDefinitionRegistries = new ArrayList<>();

    private final List<CasMultifactorWebflowCustomizer> multifactorAuthenticationFlowCustomizers = new ArrayList<>();

    protected AbstractCasMultifactorWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final Optional<FlowDefinitionRegistry> mfaFlowDefinitionRegistry,
        final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        this(flowBuilderServices,
            flowDefinitionRegistry,
            applicationContext,
            casProperties,
            mfaFlowDefinitionRegistry.map(List::of).orElseGet(List::of),
            mfaFlowCustomizers);
    }

    private AbstractCasMultifactorWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final List<FlowDefinitionRegistry> mfaFlowDefinitionRegistry,
        final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
        multifactorAuthenticationFlowDefinitionRegistries.addAll(mfaFlowDefinitionRegistry);
        multifactorAuthenticationFlowCustomizers.addAll(mfaFlowCustomizers);
    }

    @Override
    public void registerMultifactorProviderAuthenticationWebflow(final Flow flow,
                                                                 final String subflowId,
                                                                 final String providerId) {
        if (flow == null) {
            LOGGER.error("Unable to locate parent flow definition to register provider [{}]", providerId);
            return;
        }

        multifactorAuthenticationFlowDefinitionRegistries
            .stream()
            .filter(registry -> registry.containsFlowDefinition(providerId))
            .forEach(registry -> {
                val mfaFlow = (Flow) registry.getFlowDefinition(providerId);
                mfaFlow.getStartActionList().add(new ConsumerExecutionAction(WebUtils::createCredential));
                val setCredential = createSetAction("flowScope.".concat(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID), StringUtils.quote(providerId));
                mfaFlow.getStartActionList().add(setCredential);

                val initStartState = (TransitionableState) mfaFlow.getStartState();
                val transition = (Transition) initStartState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
                val targetStateId = transition.getTargetStateId();
                transition.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS));

                registerMultifactorProviderBypassAction(mfaFlow);
                registerMultifactorProviderAvailableAction(mfaFlow, targetStateId);
                registerMultifactorProviderFailureAction(flow, mfaFlow);
                registerMultifactorFlowExceptionHandlers(mfaFlow);

                val subflowState = createSubflowState(flow, providerId, providerId);
                val subflowMappings = getDefaultAttributeMappings()
                    .map(attr -> SCOPES
                        .stream()
                        .map(scope -> createFlowMapping(scope + '.' + attr, attr))
                        .collect(Collectors.toList()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

                subflowMappings.add(createFlowMapping("flowScope." + CasWebflowConstants.VAR_ID_CREDENTIAL,
                    "parent" + StringUtils.capitalize(CasWebflowConstants.VAR_ID_CREDENTIAL)));

                multifactorAuthenticationFlowCustomizers.forEach(customizer -> customizer.getWebflowAttributeMappings()
                    .forEach(key -> SCOPES.forEach(scope -> subflowMappings.add(createFlowMapping(scope + '.' + key, key)))));
                val inputMapper = createFlowInputMapper(subflowMappings);
                val subflowMapper = createSubflowAttributeMapper(inputMapper, null);
                subflowState.setAttributeMapper(subflowMapper);

                val flowMappings = getDefaultAttributeMappings()
                    .map(attr -> createFlowMapping(attr, "flowScope." + attr))
                    .collect(Collectors.toList());
                flowMappings.add(createFlowMapping("parent" + StringUtils.capitalize(CasWebflowConstants.VAR_ID_CREDENTIAL),
                    "flowScope.parent" + StringUtils.capitalize(CasWebflowConstants.VAR_ID_CREDENTIAL)));

                multifactorAuthenticationFlowCustomizers.forEach(customizer -> customizer.getWebflowAttributeMappings()
                    .forEach(key -> SCOPES.forEach(scope -> flowMappings.add(createFlowMapping(key, scope + '.' + key)))));
                createFlowInputMapper(flowMappings, mfaFlow);

                val states = getCandidateStatesForMultifactorAuthentication();
                registerMultifactorAuthenticationSubflowWithStates(flow, subflowState, states);

                registerMultifactorFlowDefinitionIntoLoginFlowRegistry();
                augmentMultifactorProviderFlowRegistry();

                LOGGER.trace("Registering the [{}] flow into the flow [{}]", providerId, flow.getId());
                val startState = flow.getTransitionableState(flow.getStartState().getId());
                createTransitionForState(startState, providerId, providerId, true);

                val initState = getState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
                createTransitionForState(initState, providerId, providerId, true);
            });
    }

    private void registerMultifactorFlowExceptionHandlers(final Flow mfaFlow) {
        createViewState(mfaFlow, CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR, CasWebflowConstants.VIEW_ID_WEBFLOW_CONFIG_ERROR);
        val handler = new TransitionExecutingFlowExecutionExceptionHandler();
        handler.add(NoMatchingTransitionException.class, CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR);
        mfaFlow.getExceptionHandlerSet().add(handler);
    }

    private static Stream<String> getDefaultAttributeMappings() {
        return Stream.of(
            CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT,
            CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION,
            CasWebflowConstants.ATTRIBUTE_TARGET_TRANSITION,
            CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT,
            CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER,
            CasWebflowConstants.ATTRIBUTE_AUTHENTICATION,
            CasWebflowConstants.ATTRIBUTE_SERVICE,
            CasWebflowConstants.ATTRIBUTE_REGISTERED_SERVICE);
    }

    private Collection<String> getCandidateStatesForMultifactorAuthentication() {
        val candidates = new LinkedHashSet<String>();
        candidates.add(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        multifactorAuthenticationFlowCustomizers.forEach(customizer ->
            candidates.addAll(customizer.getCandidateStatesForMultifactorAuthentication()));
        return candidates;
    }

    /**
     * Register flow definition into flow registry.
     */
    private void registerMultifactorFlowDefinitionIntoLoginFlowRegistry() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flowIds = registry.getFlowDefinitionIds();
            for (val flowId : flowIds) {
                val definition = registry.getFlowDefinition(flowId);
                if (definition != null) {
                    LOGGER.trace("Registering flow definition [{}]", flowId);
                    flowDefinitionRegistry.registerFlowDefinition(definition);
                }
            }
        });
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
     */
    private void augmentMultifactorProviderFlowRegistry() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flowIds = registry.getFlowDefinitionIds();
            Arrays.stream(flowIds).forEach(id -> {
                val flow = (Flow) registry.getFlowDefinition(id);
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
                            ensureEndStateTransitionExists(state, flow,
                                CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD, CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD);
                        } else {
                            LOGGER.debug("Unable to locate state definition [{}] in flow [{}]", s, flow.getId());
                        }
                    });
                }
            });
        });
    }

    private void registerMultifactorAuthenticationSubflowWithStates(final Flow flow,
                                                                    final SubflowState subflowState,
                                                                    final Collection<String> states) {
        val subflowId = subflowState.getId();
        LOGGER.trace("Candidate states for multifactor authentication are [{}]", states);
        states.forEach(stateId -> {
            LOGGER.trace("Locating state [{}] to process for multifactor authentication", stateId);
            val actionState = getState(flow, stateId);

            if (actionState == null) {
                LOGGER.error("Unable to locate state definition [{}] in flow [{}]", stateId, flow.getId());
            } else {
                LOGGER.trace("Adding transition [{}] to [{}] for [{}]",
                    CasWebflowConstants.TRANSITION_ID_DENY, CasWebflowConstants.STATE_ID_MFA_DENIED, stateId);
                createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_DENY, CasWebflowConstants.STATE_ID_MFA_DENIED);

                LOGGER.trace("Adding transition [{}] to [{}] for [{}]",
                    CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, stateId);
                createTransitionForState(actionState,
                    CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);

                LOGGER.trace(LOG_MESSAGE_TRANSITION_ID, CasWebflowConstants.TRANSITION_ID_SUCCESS, stateId);
                val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

                LOGGER.trace(LOG_MESSAGE_TRANSITION_ID, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, stateId);
                val targetWarningsId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS).getTargetStateId();

                LOGGER.trace(LOG_MESSAGE_TRANSITION_ID, CasWebflowConstants.TRANSITION_ID_DENY, stateId);
                val targetDenied = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_DENY).getTargetStateId();

                LOGGER.trace("Location transition id [{}] to process multifactor authentication for state [{}]",
                    CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, stateId);
                val targetUnavailable = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE).getTargetStateId();

                LOGGER.trace("Creating transitions to subflow state [{}]", subflowState.getId());
                val transitionSet = subflowState.getTransitionSet();
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, targetWarningsId));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_DENY, targetDenied));
                transitionSet.add(createTransition(CasWebflowConstants.STATE_ID_MFA_DENIED, targetDenied));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_UNAVAILABLE, targetUnavailable));
                transitionSet.add(createTransition(CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, targetUnavailable));
                transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_CANCEL, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));

                LOGGER.trace("Creating transition [{}] for state [{}]", subflowId, actionState.getId());
                createTransitionForState(actionState, subflowId, subflowId);
            }
        });
    }

    private void registerMultifactorProviderFailureAction(final Flow flow, final Flow mfaFlow) {
        if (flow != null) {
            val failureAction = createActionState(mfaFlow, CasWebflowConstants.STATE_ID_MFA_FAILURE,
                CasWebflowConstants.ACTION_ID_MFA_CHECK_FAILURE);
            createTransitionForState(failureAction, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE,
                CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE);
            createTransitionForState(failureAction, CasWebflowConstants.TRANSITION_ID_BYPASS,
                CasWebflowConstants.TRANSITION_ID_SUCCESS);

            LOGGER.trace("Adding end state [{}] with transition to flow [{}] for MFA",
                CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, flow.getId());
            createEndState(flow, CasWebflowConstants.STATE_ID_MFA_UNAVAILABLE, "mfa/casMfaUnavailableView");

            LOGGER.trace("Adding end state [{}] with transition flow [{}] for MFA",
                CasWebflowConstants.STATE_ID_MFA_DENIED, flow.getId());
            createEndState(flow, CasWebflowConstants.STATE_ID_MFA_DENIED, "mfa/casMfaDeniedView");
        }
    }

    private void registerMultifactorProviderAvailableAction(final Flow mfaFlow, final String targetStateId) {
        val availableAction = createActionState(mfaFlow, CasWebflowConstants.STATE_ID_MFA_CHECK_AVAILABLE,
            CasWebflowConstants.ACTION_ID_MFA_CHECK_AVAILABLE);
        if (mfaFlow.containsState(CasWebflowConstants.STATE_ID_MFA_PRE_AUTH)) {
            createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_YES,
                CasWebflowConstants.STATE_ID_MFA_PRE_AUTH);
        } else {
            createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_YES, targetStateId);
        }
        createTransitionForState(availableAction, CasWebflowConstants.TRANSITION_ID_NO, CasWebflowConstants.STATE_ID_MFA_FAILURE);
    }

    private void registerMultifactorProviderBypassAction(final Flow mfaFlow) {
        val bypassAction = createActionState(mfaFlow, CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS,
            CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS);
        createTransitionForState(bypassAction, CasWebflowConstants.TRANSITION_ID_NO, CasWebflowConstants.STATE_ID_MFA_CHECK_AVAILABLE);
        createTransitionForState(bypassAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.STATE_ID_SUCCESS);
    }
}
