package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
                                       final ApplicationContext applicationContext,
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
        val currentActions = new ArrayList<Action>();
        state.getActionList().forEach(currentActions::add);
        currentActions.forEach(a -> state.getActionList().remove(a));

        state.getActionList().add(createEvaluateAction("validateCaptchaAction"));
        currentActions.forEach(a -> state.getActionList().add(a));
        state.getTransitionSet().add(createTransition("captchaError", CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    private void createInitialRecaptchaEnabledAction(final Flow flow) {
        flow.getStartActionList().add(new Action() {
            @Override
            public Event execute(final RequestContext requestContext) {
                val googleRecaptcha = casProperties.getGoogleRecaptcha();
                WebUtils.putRecaptchaPropertiesFlowScope(requestContext, googleRecaptcha);
                return new EventFactorySupport().success(this);
            }
        });
    }
}
