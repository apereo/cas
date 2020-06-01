package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;

/**
 * This is {@link CasCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public CasCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                       final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                       final ConfigurableApplicationContext applicationContext,
                                       final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createInitialRecaptchaEnabledAction(flow);
            createValidateRecaptchaAction(flow);
        }
    }

    private void createValidateRecaptchaAction(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val actionList = state.getActionList();
        val currentActions = new ArrayList<Action>(actionList.size());
        actionList.forEach(currentActions::add);
        currentActions.forEach(actionList::remove);

        actionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA));
        currentActions.forEach(actionList::add);
        state.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    private void createInitialRecaptchaEnabledAction(final Flow flow) {
        flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_CAPTCHA));
    }
}
