package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowAttributeMapper;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@link AbstractCasMultifactorWebflowConfigurer} is responsible for
 * providing an entry point into the CAS webflow.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCasMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCasMultifactorWebflowConfigurer.class);

    public AbstractCasMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                   final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                   final ApplicationContext applicationContext,
                                                   final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    /**
     * Register flow definition into login flow registry.
     *
     * @param sourceRegistry the source registry
     */
    protected void registerMultifactorFlowDefinitionIntoLoginFlowRegistry(final FlowDefinitionRegistry sourceRegistry) {
        final String[] flowIds = sourceRegistry.getFlowDefinitionIds();
        for (final String flowId : flowIds) {
            final FlowDefinition definition = sourceRegistry.getFlowDefinition(flowId);
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
    protected void augmentMfaProviderFlowRegistry(final FlowDefinitionRegistry mfaProviderFlowRegistry) {
        final String[] flowIds = mfaProviderFlowRegistry.getFlowDefinitionIds();
        Arrays.stream(flowIds).forEach(id -> {
            final Flow flow = Flow.class.cast(mfaProviderFlowRegistry.getFlowDefinition(id));
            if (containsFlowState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT)) {
                final ActionState submit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
                ensureEndStateTransitionExists(submit, flow, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
                ensureEndStateTransitionExists(submit, flow, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
                        CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS);
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
    protected void registerMultifactorProviderAuthenticationWebflow(final Flow flow, final String subflowId,
                                                                    final FlowDefinitionRegistry mfaProviderFlowRegistry) {

        final SubflowState subflowState = createSubflowState(flow, subflowId, subflowId);

        final ActionState actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        final String targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        final String targetWarningsId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS).getTargetStateId();

        final List<DefaultMapping> mappings = new ArrayList<>();
        final Mapper inputMapper = createMapperToSubflowState(mappings);
        final SubflowAttributeMapper subflowMapper = createSubflowAttributeMapper(inputMapper, null);
        subflowState.setAttributeMapper(subflowMapper);

        subflowState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId));
        subflowState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, targetWarningsId));

        LOGGER.debug("Retrieved action state [{}]", actionState.getId());
        createTransitionForState(actionState, subflowId, subflowId);

        registerMultifactorFlowDefinitionIntoLoginFlowRegistry(mfaProviderFlowRegistry);
        augmentMfaProviderFlowRegistry(mfaProviderFlowRegistry);

        final TransitionableState state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        createTransitionForState(state, subflowId, subflowId);
    }
}
