package org.apereo.cas.acct.webflow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccountManagementRegistrationCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class AccountManagementRegistrationCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public AccountManagementRegistrationCaptchaWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null && casProperties.getAccountRegistration().getGoogleRecaptcha().isEnabled()) {

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA));
            prependActionsToActionStateExecutionList(flow, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION,
                CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_VALIDATE_CAPTCHA);
            createTransitionForState(flow, CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION,
                CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP);
        }
    }
}
