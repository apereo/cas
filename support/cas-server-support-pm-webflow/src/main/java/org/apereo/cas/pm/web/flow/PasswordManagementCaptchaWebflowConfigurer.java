package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link PasswordManagementCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public PasswordManagementCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                      final ConfigurableApplicationContext applicationContext,
                                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        val pm = casProperties.getAuthn().getPm();
        if (flow != null && pm.isEnabled() && pm.isCaptchaEnabled()) {
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA));
            
            prependActionsToActionStateExecutionList(flow,
                CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS,
                CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_CAPTCHA);
            createTransitionForState(flow, CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS,
                CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO);
        }
    }
}
