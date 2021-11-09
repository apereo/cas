package org.apereo.cas.pm.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ForgotUsernameWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ForgotUsernameWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public ForgotUsernameWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                           final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                           final ConfigurableApplicationContext applicationContext,
                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 1);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null && casProperties.getAuthn().getPm().getCore().isEnabled()) {
            createPasswordResetFlow();
        }
    }

    private void createPasswordResetFlow() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_FORGOT_USERNAME,
                CasWebflowConstants.STATE_ID_FORGOT_USERNAME_ACCT_INFO);

            val usernameInfo = createViewState(flow, CasWebflowConstants.STATE_ID_FORGOT_USERNAME_ACCT_INFO,
                "forgot-username/casForgotUsernameSendInfoView");
            createTransitionForState(usernameInfo, "findUsername",
                CasWebflowConstants.STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS);

            val sendUsernameInst = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS,
                CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION);

            createTransitionForState(sendUsernameInst, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_SENT_FORGOT_USERNAME_ACCT_INFO);
            createTransitionForState(sendUsernameInst, CasWebflowConstants.TRANSITION_ID_ERROR, usernameInfo.getId());
            createViewState(flow, CasWebflowConstants.STATE_ID_SENT_FORGOT_USERNAME_ACCT_INFO,
                "forgot-username/casForgotUsernameSentInfoView");
        }
    }
}
