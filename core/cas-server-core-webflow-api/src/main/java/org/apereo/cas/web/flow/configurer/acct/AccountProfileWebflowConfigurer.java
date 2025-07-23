package org.apereo.cas.web.flow.configurer.acct;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class AccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public AccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                           final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                           final ConfigurableApplicationContext applicationContext,
                                           final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val accountFlow = buildFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        accountFlow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET));

        val myAccountView = createViewState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, "acct/casMyAccountProfile");
        myAccountView.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE));
        createTransitionForState(myAccountView, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD, CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_REQUEST);
        createTransitionForState(myAccountView, CasWebflowConstants.TRANSITION_ID_UPDATE_SECURITY_QUESTIONS, CasWebflowConstants.STATE_ID_UPDATE_SECURITY_QUESTIONS);
        createTransitionForState(myAccountView, "deleteSession", CasWebflowConstants.STATE_ID_REMOVE_SINGLE_SIGNON_SESSION);
        createTransitionForState(myAccountView, "revokeAccessToken", CasWebflowConstants.STATE_ID_REVOKE_OIDC_ACCESS_TOKEN);

        val updateQuestions = createActionState(accountFlow, CasWebflowConstants.STATE_ID_UPDATE_SECURITY_QUESTIONS,
            CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS);
        createTransitionForState(updateQuestions, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(updateQuestions, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);

        val passwordChangeRequest = createActionState(accountFlow, CasWebflowConstants.STATE_ID_PASSWORD_CHANGE_REQUEST,
            CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST);
        createTransitionForState(passwordChangeRequest, CasWebflowConstants.TRANSITION_ID_SUCCESS, "redirectToPasswordReset");
        createEndState(accountFlow, "redirectToPasswordReset", "requestScope.url", true);

        val validate = createActionState(accountFlow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
            CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET,
            CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);

        createTransitionForState(validate, CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ);
        createTransitionForState(validate, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, myAccountView.getId());
        createStateDefaultTransition(validate, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN);

        val loginFlow = getLoginFlow();
        val serviceUrl = Strings.CI.appendIfMissing(casProperties.getServer().getPrefix(), "/").concat(accountFlow.getId());
        val view = createExternalRedirectViewFactory(String.format("'%s?%s=%s'",
            loginFlow.getId(), CasProtocolConstants.PARAMETER_SERVICE, serviceUrl));
        createEndState(accountFlow, CasWebflowConstants.STATE_ID_REDIRECT_TO_LOGIN, view);

        accountFlow.setStartState(validate);
        flowDefinitionRegistry.registerFlowDefinition(accountFlow);

        val removeSession = createActionState(accountFlow, CasWebflowConstants.STATE_ID_REMOVE_SINGLE_SIGNON_SESSION,
            CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_SINGLE_SIGNON_SESSION);
        createTransitionForState(removeSession, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(removeSession, CasWebflowConstants.TRANSITION_ID_VALIDATE, accountFlow.getStartState().getId());

        val successView = getState(loginFlow, CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS, EndState.class);
        val expression = createExpression(String.format("'%s'", accountFlow.getId()));
        successView.getEntryActionList().add(new ExternalRedirectAction(expression));

        val readStorage = createViewState(accountFlow, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ, CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_READ);
        readStorage.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE));
        createStateDefaultTransition(readStorage, accountFlow.getStartState());

        val revokeSession = createActionState(accountFlow, CasWebflowConstants.STATE_ID_REVOKE_OIDC_ACCESS_TOKEN,
            CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_OIDC_ACCESS_TOKEN);
        createTransitionForState(revokeSession, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        createTransitionForState(revokeSession, CasWebflowConstants.TRANSITION_ID_ERROR, accountFlow.getStartState().getId());
    }

}
