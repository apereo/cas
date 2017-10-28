package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public CasCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                       final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                       final ApplicationContext applicationContext,
                                       final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState state = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
            final List<Action> currentActions = new ArrayList<>();
            state.getActionList().forEach(currentActions::add);
            currentActions.forEach(a -> state.getActionList().remove(a));

            state.getActionList().add(createEvaluateAction("validateCaptchaAction"));
            currentActions.forEach(a -> state.getActionList().add(a));

            state.getTransitionSet().add(createTransition("captchaError", CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
        }
    }
}
