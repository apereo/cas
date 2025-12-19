package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link PasswordlessAuthenticationCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class PasswordlessAuthenticationCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public PasswordlessAuthenticationCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                              final FlowDefinitionRegistry flowDefinitionRegistry,
                                                              final ConfigurableApplicationContext applicationContext,
                                                              final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null && casProperties.getAuthn().getPasswordless().getGoogleRecaptcha().isEnabled()) {
            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORDLESS_INIT_CAPTCHA));
            prependActionsToActionStateExecutionList(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT,
                CasWebflowConstants.ACTION_ID_PASSWORDLESS_VALIDATE_CAPTCHA);
            createTransitionForState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT,
                CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);
        }
    }
}
