package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.ActionTransitionCriteria;
import org.springframework.webflow.engine.support.TransitionCriteriaChain;

/**
 * This is {@link SqrlWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SqrlWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            flow.getStartActionList().add(createEvaluateAction("sqrlInitialAction"));
            final ViewState state = (ViewState) flow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            final Transition def = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUBMIT);

            final TransitionCriteriaChain chain = new TransitionCriteriaChain();

            final ActionTransitionCriteria criteria = new ActionTransitionCriteria(createEvaluateAction("sqrlCleanUpAction"));
            chain.add(criteria);

            getTransitionExecutionCriteriaChainForTransition(def).forEach(chain::add);
            
            def.setExecutionCriteria(chain);
        }
    }
}
