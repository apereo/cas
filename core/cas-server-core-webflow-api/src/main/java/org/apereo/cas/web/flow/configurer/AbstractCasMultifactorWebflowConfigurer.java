package org.apereo.cas.web.flow.configurer;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The {@link AbstractCasMultifactorWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public abstract class AbstractCasMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {
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
        final var flowIds = sourceRegistry.getFlowDefinitionIds();
        for (final var flowId : flowIds) {
            final var definition = sourceRegistry.getFlowDefinition(flowId);
            LOGGER.debug("Registering flow definition [{}]", flowId);
            this.loginFlowDefinitionRegistry.registerFlowDefinition(definition);
        }
    }

    private void ensureEndStateTransitionExists(final TransitionableState state, final Flow mfaProviderFlow,
                                                final String transId, final String stateId) {
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
        final var flowIds = mfaProviderFlowRegistry.getFlowDefinitionIds();
        Arrays.stream(flowIds).forEach(id -> {
            final var flow = Flow.class.cast(mfaProviderFlowRegistry.getFlowDefinition(id));
            if (containsFlowState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT)) {
                final var states = getCandidateStatesForMultifactorAuthentication();
                states.forEach(s -> {
                    final var state = getState(flow, s);
                    ensureEndStateTransitionExists(state, flow, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
                    ensureEndStateTransitionExists(state, flow, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
                        CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS);
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
     */
    protected void registerMultifactorProviderAuthenticationWebflow(final Flow flow, final String subflowId, final FlowDefinitionRegistry mfaProviderFlowRegistry) {
        final var subflowState = createSubflowState(flow, subflowId, subflowId);
        final var states = getCandidateStatesForMultifactorAuthentication();
        LOGGER.debug("Candidate states for multifactor authentication are [{}]", states);

        states.forEach(s -> {
            LOGGER.debug("Locating state [{}] to process for multifactor authentication", s);
            final var actionState = getState(flow, s);

            LOGGER.debug("Locating transition id [{}] to process multifactor authentication for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS, s);
            final var targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

            LOGGER.debug("Locating transition id [{}] to process multifactor authentication for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, s);
            final var targetWarningsId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS).getTargetStateId();
            
            final List<DefaultMapping> mappings = new ArrayList<>();
            final var inputMapper = createMapperToSubflowState(mappings);
            final var subflowMapper = createSubflowAttributeMapper(inputMapper, null);
            subflowState.setAttributeMapper(subflowMapper);

            LOGGER.debug("Creating transitions to subflow state [{}]", subflowState.getId());
            final var transitionSet = subflowState.getTransitionSet();
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, targetWarningsId));

            LOGGER.debug("Creating transition [{}] for state [{}]", subflowId, actionState.getId());
            createTransitionForState(actionState, subflowId, subflowId);

            registerMultifactorFlowDefinitionIntoLoginFlowRegistry(mfaProviderFlowRegistry);
            augmentMultifactorProviderFlowRegistry(mfaProviderFlowRegistry);

            final var state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
            createTransitionForState(state, subflowId, subflowId);
        });
    }

    protected Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return CollectionUtils.wrapSet(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }
}
