package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ForgotUsernameCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class ForgotUsernameCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public ForgotUsernameCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                  final ConfigurableApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        val pm = casProperties.getAuthn().getPm();
        if (flow != null && pm.getCore().isEnabled() && pm.getForgotUsername().getGoogleRecaptcha().isEnabled()) {
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA));

            prependActionsToActionStateExecutionList(flow,
                CasWebflowConstants.STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS,
                CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_VALIDATE_CAPTCHA);
            createTransitionForState(flow, CasWebflowConstants.STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS,
                CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, CasWebflowConstants.STATE_ID_FORGOT_USERNAME_ACCT_INFO);
        }
    }
}
