package org.apereo.cas.web.flow.configurer.acct;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link MyAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class MyAccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Flow id for password reset.
     */
    public static final String FLOW_ID = "account";

    public MyAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                             final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                             final ConfigurableApplicationContext applicationContext,
                                             final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val accountFlow = buildFlow(FLOW_ID);
        accountFlow.getStartActionList().add(
            new ConsumerExecutionAction(context -> WebUtils.putPasswordManagementEnabled(context,
                casProperties.getAuthn().getPm().getCore().isEnabled())));
        accountFlow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET));

        val myAccountView = createViewState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, "acct/casMyAccountProfile");
        myAccountView.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE));
        createTransitionForState(myAccountView, "resetPassword", "passwordChangeRequest");

        val passwordChangeRequest = createActionState(accountFlow, "passwordChangeRequest", "accountProfilePasswordChangeRequestAction");
        createTransitionForState(passwordChangeRequest, CasWebflowConstants.TRANSITION_ID_SUCCESS, "redirectToPasswordReset");
        createEndState(accountFlow, "redirectToPasswordReset", "requestScope.url", true);

        val validate = createActionState(accountFlow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
            CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(validate, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, myAccountView.getId());
        createStateDefaultTransition(validate, "redirectToLogin");
        createEndState(accountFlow, "redirectToLogin", createExternalRedirectViewFactory(String.format("'%s'",
            casProperties.getServer().getLoginUrl())));

        accountFlow.setStartState(validate);
        mainFlowDefinitionRegistry.registerFlowDefinition(accountFlow);
    }

}
