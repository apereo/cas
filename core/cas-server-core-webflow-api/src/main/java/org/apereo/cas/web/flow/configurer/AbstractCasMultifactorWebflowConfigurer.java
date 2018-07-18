package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        val flowIds = sourceRegistry.getFlowDefinitionIds();
        for (val flowId : flowIds) {
            val definition = sourceRegistry.getFlowDefinition(flowId);
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
        val flowIds = mfaProviderFlowRegistry.getFlowDefinitionIds();
        Arrays.stream(flowIds).forEach(id -> {
            val flow = (Flow) mfaProviderFlowRegistry.getFlowDefinition(id);
            if (containsFlowState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT)) {
                val states = getCandidateStatesForMultifactorAuthentication();
                states.forEach(s -> {
                    val state = getState(flow, s);
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
        val subflowState = createSubflowState(flow, subflowId, subflowId);
        val states = getCandidateStatesForMultifactorAuthentication();
        LOGGER.debug("Candidate states for multifactor authentication are [{}]", states);

        states.forEach(s -> {
            LOGGER.debug("Locating state [{}] to process for multifactor authentication", s);
            val actionState = getState(flow, s);

            LOGGER.debug("Locating transition id [{}] to process multifactor authentication for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS, s);
            val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

            LOGGER.debug("Locating transition id [{}] to process multifactor authentication for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, s);
            val targetWarningsId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS).getTargetStateId();
            val mappings = new ArrayList<DefaultMapping>();
            val inputMapper = createMapperToSubflowState(mappings);
            val subflowMapper = createSubflowAttributeMapper(inputMapper, null);
            subflowState.setAttributeMapper(subflowMapper);

            LOGGER.debug("Creating transitions to subflow state [{}]", subflowState.getId());
            val transitionSet = subflowState.getTransitionSet();
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, targetWarningsId));

            LOGGER.debug("Creating transition [{}] for state [{}]", subflowId, actionState.getId());
            createTransitionForState(actionState, subflowId, subflowId);

            registerMultifactorFlowDefinitionIntoLoginFlowRegistry(mfaProviderFlowRegistry);
            augmentMultifactorProviderFlowRegistry(mfaProviderFlowRegistry);

            val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
            createTransitionForState(state, subflowId, subflowId);
        });
    }

    protected Collection<String> getCandidateStatesForMultifactorAuthentication() {
        return CollectionUtils.wrapSet(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }
}
